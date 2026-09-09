package dev.anvil.ade.model

enum class AgentMode(val title: String, val subtitle: String, val badge: String) {
  MODE_A("App Builder", "Natural Language -> APK On-Device", "APK Builder"),
  MODE_B("AOSP Assist", "SystemUI Design & Git Working Branch", "AOSP Assist")
}

enum class LlmProvider(val displayName: String, val defaultModel: String, val defaultEndpoint: String) {
  CLAUDE("Anthropic Claude", "claude-sonnet-4-20250514", "https://api.anthropic.com"),
  OPENAI("OpenAI", "gpt-4o-mini", "https://api.openai.com/v1"),
  OPENROUTER("OpenRouter", "anthropic/claude-sonnet-4", "https://openrouter.ai/api/v1"),
  GEMINI("Google Gemini", "gemini-2.5-flash", "https://generativelanguage.googleapis.com/v1beta"),
  GROQ("Groq", "llama-3.3-70b-versatile", "https://api.groq.com/openai/v1"),
  MISTRAL("Mistral AI", "mistral-large-latest", "https://api.mistral.ai/v1"),
  DEEPSEEK("DeepSeek", "deepseek-chat", "https://api.deepseek.com/v1"),
  TOGETHER("Together AI", "meta-llama/Llama-3.3-70B-Instruct-Turbo", "https://api.together.xyz/v1"),
  FIREWORKS("Fireworks AI", "accounts/fireworks/models/llama-v3p3-70b-instruct", "https://api.fireworks.ai/inference/v1"),
  AZURE_OPENAI("Azure OpenAI", "gpt-4o", ""),
  OLLAMA("Ollama (Local/Network)", "llama3.3", "http://localhost:11434/v1"),
  QWEN("Qwen (Alibaba DashScope)", "qwen-plus", "https://dashscope-intl.aliyuncs.com/compatible-mode/v1"),
  KIMI("Kimi (Moonshot AI)", "kimi-k2.6", "https://api.moonshot.ai/v1"),
  GLM("GLM (Zhipu / Z.ai)", "glm-4.6", "https://api.z.ai/api/paas/v4"),
  CUSTOM("Custom Endpoint", "custom-model", "");

  /** Providers with their own wire format - the endpoint field is irrelevant. */
  fun hasNativeWireFormat(): Boolean = this == CLAUDE || this == GEMINI
}

data class ProviderConfig(
  val provider: LlmProvider = LlmProvider.GEMINI,
  val apiKey: String = "",
  val model: String = provider.defaultModel,
  val endpoint: String = provider.defaultEndpoint,
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
