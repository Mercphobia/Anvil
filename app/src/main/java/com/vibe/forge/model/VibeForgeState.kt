package com.vibe.forge.model

enum class AgentMode(val title: String, val subtitle: String, val badge: String) {
  MODE_A("App Builder", "Natural Language -> APK On-Device", "APK Builder"),
  MODE_B("AOSP Assist", "SystemUI Design & Git Working Branch", "AOSP Assist")
}

enum class LlmProvider(val displayName: String, val defaultModel: String) {
  GEMINI("Google Gemini", "gemini-2.5-flash"),
  CLAUDE("Anthropic Claude", "claude-3-5-sonnet-latest"),
  OPENAI("OpenAI", "gpt-4o"),
  OPENROUTER("OpenRouter", "deepseek/deepseek-r1"),
  CUSTOM("Custom Endpoint", "custom-model")
}

data class ProviderConfig(
  val provider: LlmProvider = LlmProvider.GEMINI,
  val apiKey: String = "AIzaSy••••••••••••••••••••••••••",
  val model: String = "gemini-2.5-flash",
  val endpoint: String = "https://api.groq.com/openai/v1",
  val temperature: Float = 0.7f
) {
  fun isUsable(): Boolean = apiKey.isNotBlank()
}

enum class StepKind {
  USER,
  AGENT_TEXT,
  TOOL_CALL,
  TOOL_RESULT,
  INFO,
  ERROR
}

data class AgentStep(
  val id: String,
  val kind: StepKind,
  val text: String,
  val timestamp: String,
  val toolName: String? = null,
  val executionMs: Long? = null
)

data class ProjectFile(
  val name: String,
  val path: String,
  val isDirectory: Boolean = false,
  val content: String = "",
  val children: List<ProjectFile> = emptyList(),
  val language: String = "txt"
)

data class DiffLine(
  val type: Type,
  val text: String,
  val oldLineNo: Int? = null,
  val newLineNo: Int? = null
) {
  enum class Type { ADD, DELETE, CONTEXT, HEADER }
}
