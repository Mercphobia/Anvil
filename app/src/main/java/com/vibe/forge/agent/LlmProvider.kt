package com.vibe.forge.agent

/**
 * Supported LLM providers. Each provider defines how chat requests are
 * built and how tool calls are encoded, so the agent loop stays universal.
 */
enum class LlmProvider(
    val id: String,
    val displayName: String,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val popularModels: List<String>
) {
    CLAUDE(
        id = "claude",
        displayName = "Anthropic Claude",
        defaultBaseUrl = "https://api.anthropic.com",
        defaultModel = "claude-sonnet-4-20250514",
        popularModels = listOf(
            "claude-sonnet-4-20250514",
            "claude-opus-4-20250514",
            "claude-3-5-haiku-20241022"
        )
    ),
    OPENAI(
        id = "openai",
        displayName = "OpenAI",
        defaultBaseUrl = "https://api.openai.com/v1",
        defaultModel = "gpt-4o-mini",
        popularModels = listOf("gpt-4o", "gpt-4o-mini", "gpt-4.1", "gpt-4.1-mini")
    ),
    OPENROUTER(
        id = "openrouter",
        displayName = "OpenRouter",
        defaultBaseUrl = "https://openrouter.ai/api/v1",
        defaultModel = "anthropic/claude-sonnet-4",
        popularModels = listOf(
            "anthropic/claude-sonnet-4",
            "openai/gpt-4o",
            "google/gemini-2.5-pro",
            "meta-llama/llama-4-maverick"
        )
    ),
    GEMINI(
        id = "gemini",
        displayName = "Google Gemini",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta",
        defaultModel = "gemini-2.5-flash",
        popularModels = listOf("gemini-2.5-pro", "gemini-2.5-flash", "gemini-2.0-flash")
    ),
    CUSTOM(
        id = "custom",
        displayName = "Custom (OpenAI-compatible)",
        defaultBaseUrl = "",
        defaultModel = "",
        popularModels = emptyList()
    );

    companion object {
        fun byId(id: String): LlmProvider = entries.firstOrNull { it.id == id } ?: CLAUDE
    }
}

/**
 * Active provider configuration. Persisted via ProviderConfigStore.
 */
data class ProviderConfig(
    val provider: LlmProvider = LlmProvider.CLAUDE,
    val baseUrl: String = provider.defaultBaseUrl,
    val model: String = provider.defaultModel,
    val apiKey: String = ""
) {
    fun isUsable(): Boolean = apiKey.isNotBlank() &&
            (provider != LlmProvider.CUSTOM || (baseUrl.isNotBlank() && model.isNotBlank()))
}
