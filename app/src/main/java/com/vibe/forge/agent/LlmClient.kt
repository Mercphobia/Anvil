package com.vibe.forge.agent

import com.vibe.forge.agent.tools.ToolDefinition
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Universal LLM client. Translates the internal conversation model into
 * per-provider wire formats:
 *  - CLAUDE      -> /v1/messages (native tool_use blocks)
 *  - OPENAI/CUSTOM/OPENROUTER -> /chat/completions (function calling)
 *  - GEMINI      -> :generateContent (function declarations)
 */
class LlmClient(private val config: ProviderConfig) {

    private val gson = Gson()

    // ------------------------------------------------------------------
    // Internal conversation model
    // ------------------------------------------------------------------

    data class ToolCall(val id: String, val name: String, val inputJson: JsonObject)

    sealed class ContentBlock {
        data class Text(val text: String) : ContentBlock()
        data class ToolUse(val call: ToolCall) : ContentBlock()
        data class ToolResult(val toolCallId: String, val content: String) : ContentBlock()
    }

    data class Message(val role: String, val blocks: List<ContentBlock>) {
        companion object {
            fun user(text: String) = Message("user", listOf(ContentBlock.Text(text)))
            fun assistantText(text: String) = Message("assistant", listOf(ContentBlock.Text(text)))
        }
    }

    data class LlmResponse(
        val blocks: List<ContentBlock>,
        val stopReason: String
    )

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    suspend fun send(
        systemPrompt: String,
        messages: List<Message>,
        tools: List<ToolDefinition>
    ): Result<LlmResponse> = withContext(Dispatchers.IO) {
        try {
            when (config.provider) {
                LlmProvider.CLAUDE -> sendClaude(systemPrompt, messages, tools)
                LlmProvider.GEMINI -> sendGemini(systemPrompt, messages, tools)
                else -> sendOpenAiCompatible(systemPrompt, messages, tools)
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    // ------------------------------------------------------------------
    // Claude (/v1/messages)
    // ------------------------------------------------------------------

    private fun sendClaude(
        systemPrompt: String, messages: List<Message>, tools: List<ToolDefinition>
    ): Result<LlmResponse> {
        val body = JsonObject().apply {
            addProperty("model", config.model)
            addProperty("max_tokens", 4096)
            addProperty("system", systemPrompt)
            add("messages", JsonArray().apply {
                messages.forEach { msg ->
                    add(JsonObject().apply {
                        addProperty("role", msg.role)
                        add("content", JsonArray().apply {
                            msg.blocks.forEach { block ->
                                when (block) {
                                    is ContentBlock.Text -> add(JsonObject().apply {
                                        addProperty("type", "text")
                                        addProperty("text", block.text)
                                    })
                                    is ContentBlock.ToolUse -> add(JsonObject().apply {
                                        addProperty("type", "tool_use")
                                        addProperty("id", block.call.id)
                                        addProperty("name", block.call.name)
                                        add("input", block.call.inputJson)
                                    })
                                    is ContentBlock.ToolResult -> add(JsonObject().apply {
                                        addProperty("type", "tool_result")
                                        addProperty("tool_use_id", block.toolCallId)
                                        addProperty("content", block.content)
                                    })
                                }
                            }
                        })
                    })
                }
            })
            if (tools.isNotEmpty()) {
                add("tools", JsonArray().apply {
                    tools.forEach { tool ->
                        add(JsonObject().apply {
                            addProperty("name", tool.name)
                            addProperty("description", tool.description)
                            add("input_schema", toolSchema(tool))
                        })
                    }
                })
            }
        }

        val headers = mapOf(
            "x-api-key" to config.apiKey,
            "anthropic-version" to "2023-06-01",
            "content-type" to "application/json"
        )

        val responseText = post(config.baseUrl + "/v1/messages", headers, body.toString())
        return try {
            val root = JsonParser.parseString(responseText).asJsonObject
            val blocks = mutableListOf<ContentBlock>()
            root.getAsJsonArray("content")?.forEach { el ->
                val obj = el.asJsonObject
                when (obj.get("type")?.asString) {
                    "text" -> blocks += ContentBlock.Text(obj.get("text").asString)
                    "tool_use" -> blocks += ContentBlock.ToolUse(
                        ToolCall(
                            id = obj.get("id").asString,
                            name = obj.get("name").asString,
                            inputJson = obj.getAsJsonObject("input") ?: JsonObject()
                        )
                    )
                }
            }
            Result.success(LlmResponse(blocks, root.get("stop_reason")?.asString ?: ""))
        } catch (t: Throwable) {
            Result.failure(Exception("claude parse error: ${t.message} :: ${responseText.take(300)}"))
        }
    }

    // ------------------------------------------------------------------
    // OpenAI-compatible (/chat/completions) - OpenAI, OpenRouter, Custom
    // ------------------------------------------------------------------

    private fun sendOpenAiCompatible(
        systemPrompt: String, messages: List<Message>, tools: List<ToolDefinition>
    ): Result<LlmResponse> {
        val body = JsonObject().apply {
            addProperty("model", config.model)
            add("messages", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "system")
                    addProperty("content", systemPrompt)
                })
                messages.forEach { msg ->
                    // Flatten blocks: text becomes content; tool results -> role tool
                    val texts = msg.blocks.filterIsInstance<ContentBlock.Text>()
                    val toolUses = msg.blocks.filterIsInstance<ContentBlock.ToolUse>()
                    val toolResults = msg.blocks.filterIsInstance<ContentBlock.ToolResult>()

                    when {
                        toolResults.isNotEmpty() -> toolResults.forEach { tr ->
                            add(JsonObject().apply {
                                addProperty("role", "tool")
                                addProperty("tool_call_id", tr.toolCallId)
                                addProperty("content", tr.content)
                            })
                        }
                        toolUses.isNotEmpty() -> add(JsonObject().apply {
                            addProperty("role", "assistant")
                            add("tool_calls", JsonArray().apply {
                                toolUses.forEach { tu ->
                                    add(JsonObject().apply {
                                        addProperty("id", tu.call.id)
                                        addProperty("type", "function")
                                        add("function", JsonObject().apply {
                                            addProperty("name", tu.call.name)
                                            addProperty("arguments", tu.call.inputJson.toString())
                                        })
                                    })
                                }
                            })
                        })
                        else -> add(JsonObject().apply {
                            addProperty("role", msg.role)
                            addProperty("content", texts.joinToString("\n") { it.text })
                        })
                    }
                }
            })
            if (tools.isNotEmpty()) {
                add("tools", JsonArray().apply {
                    tools.forEach { tool ->
                        add(JsonObject().apply {
                            addProperty("type", "function")
                            add("function", JsonObject().apply {
                                addProperty("name", tool.name)
                                addProperty("description", tool.description)
                                add("parameters", toolSchema(tool))
                            })
                        })
                    }
                })
            }
        }

        val headers = mutableMapOf("content-type" to "application/json")
        if (config.provider == LlmProvider.OPENROUTER) {
            headers["Authorization"] = "Bearer " + config.apiKey
            headers["HTTP-Referer"] = "https://vibe.forge"
        } else {
            headers["Authorization"] = "Bearer " + config.apiKey
        }

        val url = config.baseUrl.trimEnd('/') + "/chat/completions"
        val responseText = post(url, headers, body.toString())
        return try {
            val root = JsonParser.parseString(responseText).asJsonObject
            val choice = root.getAsJsonArray("choices").first().asJsonObject
            val message = choice.getAsJsonObject("message")
            val blocks = mutableListOf<ContentBlock>()
            message.get("content")?.let {
                if (!it.isJsonNull) blocks += ContentBlock.Text(it.asString)
            }
            message.getAsJsonArray("tool_calls")?.forEach { el ->
                val obj = el.asJsonObject
                val fn = obj.getAsJsonObject("function")
                val args = try {
                    JsonParser.parseString(fn.get("arguments").asString).asJsonObject
                } catch (t: Throwable) {
                    JsonObject()
                }
                blocks += ContentBlock.ToolUse(
                    ToolCall(obj.get("id").asString, fn.get("name").asString, args)
                )
            }
            Result.success(LlmResponse(blocks, choice.get("finish_reason")?.asString ?: ""))
        } catch (t: Throwable) {
            Result.failure(Exception("openai parse error: ${t.message} :: ${responseText.take(300)}"))
        }
    }

    // ------------------------------------------------------------------
    // Gemini (:generateContent)
    // ------------------------------------------------------------------

    private fun sendGemini(
        systemPrompt: String, messages: List<Message>, tools: List<ToolDefinition>
    ): Result<LlmResponse> {
        val body = JsonObject().apply {
            add("systemInstruction", JsonObject().apply {
                add("parts", JsonArray().apply {
                    add(JsonObject().apply { addProperty("text", systemPrompt) })
                })
            })
            add("contents", JsonArray().apply {
                messages.forEach { msg ->
                    add(JsonObject().apply {
                        addProperty("role", if (msg.role == "assistant") "model" else "user")
                        add("parts", JsonArray().apply {
                            msg.blocks.forEach { block ->
                                when (block) {
                                    is ContentBlock.Text -> add(JsonObject().apply {
                                        addProperty("text", block.text)
                                    })
                                    is ContentBlock.ToolUse -> add(JsonObject().apply {
                                        add("functionCall", JsonObject().apply {
                                            addProperty("name", block.call.name)
                                            add("args", block.call.inputJson)
                                        })
                                    })
                                    is ContentBlock.ToolResult -> add(JsonObject().apply {
                                        add("functionResponse", JsonObject().apply {
                                            addProperty("name", block.toolCallId)
                                            add("response", JsonObject().apply {
                                                addProperty("result", block.content)
                                            })
                                        })
                                    })
                                }
                            }
                        })
                    })
                }
            })
            if (tools.isNotEmpty()) {
                add("tools", JsonArray().apply {
                    add(JsonObject().apply {
                        add("functionDeclarations", JsonArray().apply {
                            tools.forEach { tool ->
                                add(JsonObject().apply {
                                    addProperty("name", tool.name)
                                    addProperty("description", tool.description)
                                    add("parameters", toolSchema(tool))
                                })
                            }
                        })
                    })
                })
            }
        }

        val url = config.baseUrl.trimEnd('/') + "/models/" + config.model +
                ":generateContent?key=" + config.apiKey
        val responseText = post(url, mapOf("content-type" to "application/json"), body.toString())
        return try {
            val root = JsonParser.parseString(responseText).asJsonObject
            val candidate = root.getAsJsonArray("candidates").first().asJsonObject
            val blocks = mutableListOf<ContentBlock>()
            candidate.getAsJsonObject("content")?.getAsJsonArray("parts")?.forEach { el ->
                val obj = el.asJsonObject
                when {
                    obj.has("text") -> blocks += ContentBlock.Text(obj.get("text").asString)
                    obj.has("functionCall") -> {
                        val fc = obj.getAsJsonObject("functionCall")
                        blocks += ContentBlock.ToolUse(
                            ToolCall(
                                id = fc.get("name").asString,
                                name = fc.get("name").asString,
                                inputJson = fc.getAsJsonObject("args") ?: JsonObject()
                            )
                        )
                    }
                }
            }
            Result.success(LlmResponse(blocks, candidate.get("finishReason")?.asString ?: ""))
        } catch (t: Throwable) {
            Result.failure(Exception("gemini parse error: ${t.message} :: ${responseText.take(300)}"))
        }
    }

    // ------------------------------------------------------------------
    // Shared helpers
    // ------------------------------------------------------------------

    private fun toolSchema(tool: ToolDefinition): JsonObject {
        val schema = JsonObject()
        schema.addProperty("type", "object")
        val props = JsonObject()
        tool.properties.forEach { (name, param) ->
            props.add(name, JsonObject().apply {
                addProperty("type", param.type)
                addProperty("description", param.description)
                if (param.type == "array" && param.items != null) {
                    add("items", JsonObject().apply { addProperty("type", param.items) })
                }
            })
        }
        schema.add("properties", props)
        schema.add("required", JsonArray().apply {
            tool.required.forEach { add(it) }
        })
        return schema
    }

    private fun post(url: String, headers: Map<String, String>, body: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 20000
            conn.readTimeout = 120000
            conn.doOutput = true
            headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
            OutputStreamWriter(conn.outputStream).use { it.write(body) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.readText() ?: ""
            if (code !in 200..299) {
                throw Exception("HTTP $code: ${text.take(400)}")
            }
            text
        } finally {
            conn.disconnect()
        }
    }
}
