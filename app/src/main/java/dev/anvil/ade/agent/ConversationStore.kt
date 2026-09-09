package dev.anvil.ade.agent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Persists agent conversation history per project so sessions survive
 * app restarts. Stored at <project>/.anvil/conversation.json
 * (separate from memory.md - this is the raw chat, trimmed to a cap).
 */
class ConversationStore(projectRoot: File) {

    private val file = File(projectRoot, ".anvil/conversation.json")
    private val maxMessages = 60

    suspend fun load(): List<LlmClient.Message> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) return@withContext emptyList()
            val root = JSONObject(file.readText())
            val arr = root.getJSONArray("messages")
            val messages = mutableListOf<LlmClient.Message>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val role = obj.getString("role")
                val blocks = mutableListOf<LlmClient.ContentBlock>()
                val blockArr = obj.getJSONArray("blocks")
                for (j in 0 until blockArr.length()) {
                    val b = blockArr.getJSONObject(j)
                    when (b.getString("type")) {
                        "text" -> blocks += LlmClient.ContentBlock.Text(b.getString("text"))
                        "tool_use" -> blocks += LlmClient.ContentBlock.ToolUse(
                            LlmClient.ToolCall(
                                id = b.getString("id"),
                                name = b.getString("name"),
                                inputJson = com.google.gson.JsonParser.parseString(
                                    b.getString("input")).asJsonObject
                            )
                        )
                        "tool_result" -> blocks += LlmClient.ContentBlock.ToolResult(
                            b.getString("tool_call_id"), b.getString("content")
                        )
                    }
                }
                messages += LlmClient.Message(role, blocks)
            }
            messages
        } catch (t: Throwable) {
            emptyList()
        }
    }

    suspend fun save(messages: List<LlmClient.Message>) = withContext(Dispatchers.IO) {
        try {
            file.parentFile?.mkdirs()
            val trimmed = messages.takeLast(maxMessages)
            val arr = JSONArray()
            trimmed.forEach { msg ->
                val obj = JSONObject()
                obj.put("role", msg.role)
                val blockArr = JSONArray()
                msg.blocks.forEach { block ->
                    val b = JSONObject()
                    when (block) {
                        is LlmClient.ContentBlock.Text -> {
                            b.put("type", "text")
                            b.put("text", block.text)
                        }
                        is LlmClient.ContentBlock.ToolUse -> {
                            b.put("type", "tool_use")
                            b.put("id", block.call.id)
                            b.put("name", block.call.name)
                            b.put("input", block.call.inputJson.toString())
                        }
                        is LlmClient.ContentBlock.ToolResult -> {
                            b.put("type", "tool_result")
                            b.put("tool_call_id", block.toolCallId)
                            b.put("content", block.content)
                        }
                    }
                    blockArr.put(b)
                }
                obj.put("blocks", blockArr)
                arr.put(obj)
            }
            val root = JSONObject()
            root.put("messages", arr)
            file.writeText(root.toString())
        } catch (t: Throwable) {
            // persistence is best-effort
        }
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        try {
            file.delete()
        } catch (t: Throwable) {
        }
    }
}
