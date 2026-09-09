package dev.anvil.ade.agent

/**
 * LLM provider catalog. Providers with hasNativeWireFormat()==false all speak
 * the OpenAI-compatible /chat/completions wire format and share
 * LlmClient.sendOpenAiCompatible(); only the base URL and model differ.
 */
enum class LlmProvider(
    val id: String,
    val displayName: String,
    val defaultBaseUrl: String,
    val defaultModel: String
) {
    CLAUDE(
        id = "claude", displayName = "Anthropic Claude",
        defaultBaseUrl = "https://api.anthropic.com",
        defaultModel = "claude-sonnet-4-20250514"
    ),
    OPENAI(
        id = "openai", displayName = "OpenAI",
        defaultBaseUrl = "https://api.openai.com/v1",
        defaultModel = "gpt-4o-mini"
    ),
    OPENROUTER(
        id = "openrouter", displayName = "OpenRouter",
        defaultBaseUrl = "https://openrouter.ai/api/v1",
        defaultModel = "anthropic/claude-sonnet-4"
    ),
    GEMINI(
        id = "gemini", displayName = "Google Gemini",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta",
        defaultModel = "gemini-2.5-flash"
    ),
    // ---- OpenAI-compatible providers ----
    GROQ(
        id = "groq", displayName = "Groq",
        defaultBaseUrl = "https://api.groq.com/openai/v1",
        defaultModel = "llama-3.3-70b-versatile"
    ),
    MISTRAL(
        id = "mistral", displayName = "Mistral AI",
        defaultBaseUrl = "https://api.mistral.ai/v1",
        defaultModel = "mistral-large-latest"
    ),
    DEEPSEEK(
        id = "deepseek", displayName = "DeepSeek",
        defaultBaseUrl = "https://api.deepseek.com/v1",
        defaultModel = "deepseek-chat"
    ),
    TOGETHER(
        id = "together", displayName = "Together AI",
        defaultBaseUrl = "https://api.together.xyz/v1",
        defaultModel = "meta-llama/Llama-3.3-70B-Instruct-Turbo"
    ),
    FIREWORKS(
        id = "fireworks", displayName = "Fireworks AI",
        defaultBaseUrl = "https://api.fireworks.ai/inference/v1",
        defaultModel = "accounts/fireworks/models/llama-v3p3-70b-instruct"
    ),
    AZURE_OPENAI(
        id = "azure_openai", displayName = "Azure OpenAI",
        defaultBaseUrl = "", // must be set manually: https://<resource>.openai.azure.com/openai/deployments/<deployment>
        defaultModel = "gpt-4o"
    ),
    OLLAMA(
        id = "ollama", displayName = "Ollama (Local/Network)",
        defaultBaseUrl = "http://localhost:11434/v1",
        defaultModel = "llama3.3"
    ),
    QWEN(
        id = "qwen", displayName = "Qwen (Alibaba DashScope)",
        defaultBaseUrl = "https://dashscope-intl.aliyuncs.com/compatible-mode/v1",
        defaultModel = "qwen-plus"
    ),
    KIMI(
        id = "kimi", displayName = "Kimi (Moonshot AI)",
        defaultBaseUrl = "https://api.moonshot.ai/v1",
        defaultModel = "kimi-k2.6"
    ),
    GLM(
        id = "glm", displayName = "GLM (Zhipu / Z.ai)",
        defaultBaseUrl = "https://api.z.ai/api/paas/v4",
        defaultModel = "glm-4.6"
    ),
    CUSTOM(
        id = "custom", displayName = "Custom (OpenAI-compatible)",
        defaultBaseUrl = "", defaultModel = ""
    );

    /** Providers with their own wire format (not /chat/completions generic). */
    fun hasNativeWireFormat(): Boolean = this == CLAUDE || this == GEMINI

    companion object {
        fun byId(id: String): LlmProvider = entries.firstOrNull { it.id == id } ?: CLAUDE
    }
}

data class ProviderConfig(
    val provider: LlmProvider = LlmProvider.CLAUDE,
    val baseUrl: String = provider.defaultBaseUrl,
    val model: String = provider.defaultModel,
    val apiKey: String = ""
) {
    fun isUsable(): Boolean = apiKey.isNotBlank() &&
            (provider.hasNativeWireFormat() || (baseUrl.isNotBlank() && model.isNotBlank()))
}
