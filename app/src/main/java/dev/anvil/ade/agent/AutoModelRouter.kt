package dev.anvil.ade.agent

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Intelligent model router: selects the optimal LLM provider + model
 * based on task complexity and user-configured provider availability.
 *
 * Philosophy:
 *   - Simple edits (one file, short instruction) → cheapest usable model
 *   - Medium tasks (2-3 files, moderate logic) → balanced cost/quality
 *   - Complex tasks (architecture, multi-file, debugging) → best model
 *
 * Routing strategy:
 *   1. Classify instruction complexity (SIMPLE / MEDIUM / COMPLEX)
 *   2. Filter to user's configured + API-key-provided providers
 *   3. Select the best model for that tier
 *   4. Fall back progressively if the preferred is unavailable
 *
 * Model tiers are defined per-provider and kept in sync with
 * the LlmProvider enum + known model catalogs.
 */
class AutoModelRouter(
    private val configStore: ProviderConfigStore,
    private val appContext: android.content.Context
) {
    enum class Complexity {
        SIMPLE,
        MEDIUM,
        COMPLEX
    }

    data class Route(
        val provider: LlmProvider,
        val model: String,
        val complexity: Complexity,
        val reason: String
    )

    data class Classification(
        val complexity: Complexity,
        val fileCount: Int,
        val instructionLength: Int,
        val hasBuild: Boolean,
        val hasDebug: Boolean,
        val hasMultiFile: Boolean,
        val hasArchitecture: Boolean,
        val score: Double
    )

    /** Heuristic keywords that indicate complex tasks. */
    private val complexKeywords = setOf(
        "architecture", "refactor", "redesign", "design system",
        "from scratch", "migrate", "rewrite", "restructure",
        "implement pattern", "dependency injection", "clean architecture",
        "multi-module", "plugin system", "event bus", "microservice"
    )

    /** Heuristic keywords that indicate medium complexity. */
    private val mediumKeywords = setOf(
        "add feature", "modify", "update", "integrate",
        "connect", "wire up", "implement", "build",
        "create component", "add screen", "add page",
        "add endpoint", "add viewmodel", "fix multiple"
    )

    /** Keywords suggesting build/compile step needed. */
    private val buildKeywords = setOf(
        "build", "compile", "run", "test", "deploy",
        "package", "assemble", "install", "gradle"
    )

    /** Keywords suggesting debugging. */
    private val debugKeywords = setOf(
        "debug", "fix", "error", "bug", "crash", "broken",
        "not working", "issue", "problem", "failing", "stacktrace"
    )

    // Model tiers (cheapest → best) — ordered by provider + model
    private data class TierModel(val providerId: String, val model: String, val tier: Int)

    /**
     * Tier 1 = premium (COMPLEX)
     * Tier 2 = balanced (MEDIUM)
     * Tier 3 = cheap   (SIMPLE)
     */
    private val modelTiers = listOf(
        // --- Tier 1: Premium models for complex tasks ---
        TierModel("claude",       "claude-opus-4-20250514",    1),
        TierModel("openai",       "o4-mini",                    1),
        TierModel("openai",       "gpt-4o",                     1),
        TierModel("openrouter",   "anthropic/claude-sonnet-4",  1),
        TierModel("gemini",       "gemini-2.5-pro",             1),
        TierModel("mistral",      "mistral-large-latest",       1),
        TierModel("groq",         "llama-4-maverick",           1),
        TierModel("deepseek",     "deepseek-reasoner",          1),

        // --- Tier 2: Balanced models ---
        TierModel("claude",       "claude-sonnet-4-20250514",   2),
        TierModel("openai",       "gpt-4.1",                    2),
        TierModel("openrouter",   "openai/gpt-4o",              2),
        TierModel("gemini",       "gemini-2.5-flash",           2),
        TierModel("deepseek",     "deepseek-chat",              2),
        TierModel("groq",         "llama-3.3-70b-versatile",   2),
        TierModel("qwen",         "qwen-plus",                  2),

        // --- Tier 3: Budget models ---
        TierModel("openai",       "gpt-4o-mini",                3),
        TierModel("openrouter",   "openai/gpt-4o-mini",         3),
        TierModel("openai",       "gpt-4.1-mini",               3),
        TierModel("claude",       "claude-haiku-3.5",           3),
        TierModel("gemini",       "gemini-2.5-flash",           3),
        TierModel("groq",         "llama-3.3-70b-versatile",   3),
        TierModel("ollama",       "llama3.3",                   3),
    )

    private val _lastRoute = MutableStateFlow<Route?>(null)
    val lastRoute: StateFlow<Route?> = _lastRoute.asStateFlow()

    private val _lastClassification = MutableStateFlow<Classification?>(null)
    val lastClassification: StateFlow<Classification?> = _lastClassification.asStateFlow()

    private val _routingHistory = MutableStateFlow<List<Route>>(emptyList())
    val routingHistory: StateFlow<List<Route>> = _routingHistory.asStateFlow()

    // -----------------------------------------------------------------
    // COMPLEXITY CLASSIFICATION
    // -----------------------------------------------------------------

    /**
     * Analyze the user instruction and workspace state to classify complexity.
     *
     * Factors:
     *   - Instruction length (token estimate)
     *   - File count referenced
     *   - Keyword signals (build, debug, architecture)
     *   - Whether multiple files are explicitly mentioned
     */
    fun classifyComplexity(
        instruction: String,
        filePaths: List<String> = emptyList()
    ): Classification {
        val text = instruction.lowercase()
        val instructionLength = instruction.length
        val fileCount = filePaths.size

        val hasBuild = buildKeywords.any { text.contains(it) }
        val hasDebug = debugKeywords.any { text.contains(it) }
        val hasArchitecture = complexKeywords.any { text.contains(it) }
        val hasMultiFile = fileCount > 1 ||
                Regex("""files?\s*[:;]\s*\n""").containsMatchIn(instruction) ||
                instruction.lines().count { it.trimStart().startsWith("-") } >= 2

        // Scoring: weighted heuristic
        var score = 0.0

        // Length: longer = more complex
        score += (instructionLength / 200.0).coerceAtMost(5.0)

        // File count: more files = more complex
        score += fileCount * 1.5

        // Keywords
        if (hasArchitecture) score += 4.0
        if (hasBuild) score += 1.5
        if (hasDebug) score += 2.0
        if (hasMultiFile) score += 2.0

        // Medium keywords (cumulative)
        val mediumCount = mediumKeywords.count { text.contains(it) }
        score += mediumCount * 0.5

        val complexity = when {
            score >= 6.0 -> Complexity.COMPLEX
            score >= 2.5 -> Complexity.MEDIUM
            else         -> Complexity.SIMPLE
        }

        val classification = Classification(
            complexity = complexity,
            fileCount = fileCount,
            instructionLength = instructionLength,
            hasBuild = hasBuild,
            hasDebug = hasDebug,
            hasMultiFile = hasMultiFile,
            hasArchitecture = hasArchitecture,
            score = score
        )

        _lastClassification.value = classification
        return classification
    }

    // -----------------------------------------------------------------
    // ROUTING
    // -----------------------------------------------------------------

    /**
     * Select the best available provider+model for the given complexity.
     *
     * Strategy:
     *   COMPLEX → tier 1 (premium), fall back to tier 2, then tier 3
     *   MEDIUM  → tier 2 (balanced), fall back to tier 1 or 3
     *   SIMPLE  → tier 3 (cheap), fall back to tier 2
     *
     * Only considers providers for which the user has configured an API key.
     */
    fun route(
        complexity: Complexity,
        preferredProvider: LlmProvider? = null
    ): Route {
        val configuredProviders = configuredProviders()
        if (configuredProviders.isEmpty()) {
            val fallback = Route(
                provider = LlmProvider.CLAUDE,
                model = "claude-sonnet-4-20250514",
                complexity = complexity,
                reason = "no configured providers — falling back to default"
            )
            _lastRoute.value = fallback
            _routingHistory.value = (_routingHistory.value + fallback).takeLast(100)
            return fallback
        }

        val configuredIds = configuredProviders.map { it.id }.toSet()

        // Determine preferred tiers for this complexity
        val tiers = when (complexity) {
            Complexity.COMPLEX -> listOf(1, 2, 3)
            Complexity.MEDIUM  -> listOf(2, 1, 3)
            Complexity.SIMPLE  -> listOf(3, 2, 1)
        }

        // If user has a preferred provider, prioritize it
        val sortedTiers = modelTiers.sortedBy { tier ->
            // Boost the preferred provider's models within their tier
            if (tier.providerId == preferredProvider?.id) -1000 else 0
        }

        for (targetTier in tiers) {
            val candidates = sortedTiers.filter { it.tier == targetTier && it.providerId in configuredIds }
            if (candidates.isNotEmpty()) {
                val selected = if (preferredProvider != null) {
                    candidates.firstOrNull { it.providerId == preferredProvider.id }
                        ?: candidates.first()
                } else {
                    candidates.first()
                }

                val route = Route(
                    provider = LlmProvider.byId(selected.providerId),
                    model = selected.model,
                    complexity = complexity,
                    reason = when (targetTier) {
                        1 -> "complex task — premium model"
                        2 -> "balanced task — mid-tier model"
                        3 -> "simple task — budget model"
                        else -> "auto-selected"
                    }
                )
                _lastRoute.value = route
                _routingHistory.value = (_routingHistory.value + route).takeLast(100)
                return route
            }
        }

        // Ultimate fallback
        val fallback = Route(
            provider = configuredProviders.first(),
            model = configuredProviders.first().defaultModel,
            complexity = complexity,
            reason = "no tier match — using first configured provider"
        )
        _lastRoute.value = fallback
        _routingHistory.value = (_routingHistory.value + fallback).takeLast(100)
        return fallback
    }

    /**
     * Full pipeline: classify → route in one call.
     */
    fun routeInstruction(
        instruction: String,
        filePaths: List<String> = emptyList(),
        preferredProvider: LlmProvider? = null
    ): Route {
        val classification = classifyComplexity(instruction, filePaths)
        return route(classification.complexity, preferredProvider)
    }

    // -----------------------------------------------------------------
    // PROVIDER DISCOVERY
    // -----------------------------------------------------------------

    /**
     * Returns all providers for which the user has saved an API key.
     * Falls back to the single active provider if config isn't multi-provider.
     */
    fun configuredProviders(): List<LlmProvider> {
        return try {
            val config = configStore.load(appContext)
            if (config.isUsable()) listOf(config.provider)
            else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // -----------------------------------------------------------------
    // COST ESTIMATION HELPER
    // -----------------------------------------------------------------

    /**
     * Rough cost estimate for routing this instruction.
     * Does NOT make API calls — purely heuristic.
     */
    fun estimateCost(instruction: String, complexity: Complexity): String {
        val inputTokens = instruction.length / 4
        val outputMultiplier = when (complexity) {
            Complexity.SIMPLE  -> 1.0
            Complexity.MEDIUM  -> 2.5
            Complexity.COMPLEX -> 5.0
        }
        val outputTokens = (inputTokens * outputMultiplier).toInt()

        val tier = when (complexity) {
            Complexity.SIMPLE  -> 0.20  // ~$0.20/1M input
            Complexity.MEDIUM  -> 0.60  // ~$0.60/1M input
            Complexity.COMPLEX -> 2.50  // ~$2.50/1M input
        }
        val cost = (inputTokens.toDouble() / 1_000_000.0) * tier +
                (outputTokens.toDouble() / 1_000_000.0) * (tier * 4)

        return if (cost < 0.001) "< \$0.001"
        else String.format("\$%.4f", cost)
    }

    // -----------------------------------------------------------------
    // MANAGEMENT
    // -----------------------------------------------------------------

    fun reset() {
        _lastRoute.value = null
        _lastClassification.value = null
        _routingHistory.value = emptyList()
    }

    /** Force a specific route (manual override). */
    fun forceRoute(provider: LlmProvider, model: String, complexity: Complexity) {
        val route = Route(
            provider = provider,
            model = model,
            complexity = complexity,
            reason = "manual override"
        )
        _lastRoute.value = route
        _routingHistory.value = (_routingHistory.value + route).takeLast(100)
    }
}