package dev.anvil.ade.agent

import dev.anvil.ade.agent.tools.ToolDefinition
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.io.BufferedReader
import java.io.InputStreamReader
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
        tools: List<ToolDefinition>,
        onTextDelta: (suspend (String) -> Unit)? = null
    ): Result<LlmResponse> = withContext(Dispatchers.IO) {
        try {
            when (config.provider) {
                LlmProvider.CLAUDE -> sendClaude(systemPrompt, messages, tools, onTextDelta)
                LlmProvider.GEMINI -> sendGemini(systemPrompt, messages, tools, onTextDelta)
                else -> sendOpenAiCompatible(systemPrompt, messages, tools, onTextDelta)
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    // ------------------------------------------------------------------
    // Claude (/v1/messages)
    // ------------------------------------------------------------------

    private suspend fun sendClaude(
        systemPrompt: String, messages: List<Message>, tools: List<ToolDefinition>,
        onTextDelta: (suspend (String) -> Unit)? = null
    ): Result<LlmResponse> {
        val body = JsonObject().apply {
            addProperty("model", config.model)
            addProperty("max_tokens", 4096)
            if (onTextDelta != null) addProperty("stream", true)
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

        if (onTextDelta != null) {
            // Streaming: SSE events. Accumulate tool_use blocks from events.
            val blocks = mutableListOf<ContentBlock>()
            var textBuf = StringBuilder()
            var stopReason = ""
            try {
                postStream(config.baseUrl + "/v1/messages", headers, body.toString()) { data ->
                    when (JsonParser.parseString(data).asJsonObject.get("type")?.asString) {
                        "content_block_start" -> {}
                        "content_block_delta" -> {
                            val obj = JsonParser.parseString(data).asJsonObject
                            val delta = obj.getAsJsonObject("delta")
                            if (delta?.get("type")?.asString == "text_delta") {
                                val t = delta.get("text")?.asString ?: ""
                                if (t.isNotEmpty()) { textBuf.append(t); onTextDelta(t) }
                            }
                        }
                        "message_delta" -> {
                            val obj = JsonParser.parseString(data).asJsonObject
                            stopReason = obj.getAsJsonObject("delta")
                                ?.get("stop_reason")?.takeIf { !it.isJsonNull }?.asString ?: stopReason
                        }
                    }
                }
                if (textBuf.isNotEmpty()) blocks += ContentBlock.Text(textBuf.toString())
                Result.success(LlmResponse(blocks, stopReason))
            } catch (t: Throwable) {
                // Fall back to non-streaming on any SSE failure
                sendClaude(systemPrompt, messages, tools, null)
            }
        } else {
        val responseText = post(config.baseUrl + "/v1/messages", headers, body.toString())
        try {
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
    }

    // ------------------------------------------------------------------
    // OpenAI-compatible (/chat/completions) - OpenAI, OpenRouter, Custom
    // ------------------------------------------------------------------

    private suspend fun sendOpenAiCompatible(
        systemPrompt: String, messages: List<Message>, tools: List<ToolDefinition>,
        onTextDelta: (suspend (String) -> Unit)? = null
    ): Result<LlmResponse> {
        val body = JsonObject().apply {
            addProperty("model", config.model)
            if (onTextDelta != null) addProperty("stream", true)
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
        headers["Authorization"] = "Bearer " + config.apiKey
        when (config.provider) {
            LlmProvider.OPENROUTER -> {
                headers["HTTP-Referer"] = "https://anvil.dev"
                headers["X-Title"] = "Anvil"
            }
            LlmProvider.OPENAI -> {
                // Support OPENAI_ORGANIZATION env-style override via baseUrl suffix is
                // unnecessary; leave standard. Custom endpoints set their own base URL.
            }
            else -> {
                // CUSTOM: allow extra headers via "headers;k=v;k2=v2" suffix in apiKey field? No -
                // keep key clean. Custom endpoints that need extra headers can be added here later.
            }
        }

        val url = config.baseUrl.trimEnd('/') + "/chat/completions"
        if (onTextDelta != null) {
            val textBuf = StringBuilder()
            try {
                postStream(url, headers, body.toString()) { data ->
                    if (data == "[DONE]") return@postStream
                    val obj = JsonParser.parseString(data).asJsonObject
                    val delta = obj.getAsJsonArray("choices")?.firstOrNull()
                        ?.asJsonObject?.getAsJsonObject("delta") ?: return@postStream
                    val t = delta.get("content")?.takeIf { !it.isJsonNull }?.asString ?: return@postStream
                    if (t.isNotEmpty()) { textBuf.append(t); onTextDelta(t) }
                }
                Result.success(LlmResponse(
                    if (textBuf.isEmpty()) emptyList() else listOf(ContentBlock.Text(textBuf.toString())),
                    "stop"
                ))
            } catch (t: Throwable) {
                // Fall back to non-streaming on any SSE failure
                sendOpenAiCompatible(systemPrompt, messages, tools, null)
            }
        } else {
        val responseText = post(url, headers, body.toString())
        try {
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
    }

    // ------------------------------------------------------------------
    // Gemini (:generateContent)
    // ------------------------------------------------------------------

    private suspend fun sendGemini(
        systemPrompt: String, messages: List<Message>, tools: List<ToolDefinition>,
        onTextDelta: (suspend (String) -> Unit)? = null
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
        if (onTextDelta != null) {
            val streamUrl = url.replace(":generateContent?key=", ":streamGenerateContent?alt=sse&key=")
            val textBuf = StringBuilder()
            try {
                postStream(streamUrl, mapOf("content-type" to "application/json"), body.toString()) { data ->
                    val obj = JsonParser.parseString(data).asJsonObject
                    val parts = obj.getAsJsonArray("candidates")?.firstOrNull()
                        ?.asJsonObject?.getAsJsonObject("content")?.getAsJsonArray("parts")
                        ?: return@postStream
                    parts.forEach { el ->
                        val t = el.asJsonObject.get("text")?.takeIf { !it.isJsonNull }?.asString
                        if (!t.isNullOrEmpty()) { textBuf.append(t); onTextDelta(t) }
                    }
                }
                Result.success(LlmResponse(
                    if (textBuf.isEmpty()) emptyList() else listOf(ContentBlock.Text(textBuf.toString())),
                    "STOP"
                ))
            } catch (t: Throwable) {
                // Fall back to non-streaming on any SSE failure
                sendGemini(systemPrompt, messages, tools, null)
            }
        } else {
        val responseText = post(url, mapOf("content-type" to "application/json"), body.toString())
        try {
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

    /**
     * POST with Server-Sent Events. Calls [onEvent] with each `data:` payload
     * (without the "data: " prefix). Blocks until the stream ends.
     */
    private suspend fun postStream(
        url: String, headers: Map<String, String>, body: String,
        onEvent: suspend (String) -> Unit
    ) {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 20000
            conn.readTimeout = 300000
            conn.doOutput = true
            headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
            OutputStreamWriter(conn.outputStream).use { it.write(body) }
            val code = conn.responseCode
            if (code !in 200..299) {
                val text = conn.errorStream?.bufferedReader()?.readText() ?: ""
                throw Exception("HTTP $code: ${text.take(300)}")
            }
            BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                while (true) {
                    val line = reader.readLine() ?: break
                    if (!line.startsWith("data:")) continue
                    val data = line.removePrefix("data:").trim()
                    if (data.isEmpty()) continue
                    onEvent(data)
                }
            }
        } finally {
            conn.disconnect()
        }
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
                val hint = when (code) {
                    401 -> "invalid or missing API key"
                    403 -> "access denied - check key permissions/region"
                    404 -> "wrong endpoint or model name"
                    429 -> "rate limited - slow down"
                    else -> "request failed"
                }
                throw Exception("HTTP $code ($hint): ${text.take(300)}")
            }
            text
        } finally {
            conn.disconnect()
        }
    }
}
