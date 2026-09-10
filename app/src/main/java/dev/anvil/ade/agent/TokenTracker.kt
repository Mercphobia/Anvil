package dev.anvil.ade.agent

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Per-session token accumulation and cost estimation.
 *
 * Tracks input and output tokens across all LLM calls in a session.
 * Exposes totals via StateFlow for real-time UI binding (e.g., a
 * cost counter in the status bar).
 *
 * Pricing data is approximate — pulled from provider docs as of late 2025.
 * Users can override rates via [setCustomRates].
 *
 * Thread-safe: all state mutations are atomic via MutableStateFlow.update {}.
 */
class TokenTracker {

    data class Totals(
        val inputTokens: Long = 0L,
        val outputTokens: Long = 0L,
        val estimatedCostUsd: Double = 0.0
    ) {
        val totalTokens: Long get() = inputTokens + outputTokens

        companion object {
            val ZERO = Totals()
        }
    }

    /** Per-model pricing: USD per 1M tokens. */
    data class Pricing(
        val inputPerMillion: Double,
        val outputPerMillion: Double
    )

    /** Built-in pricing for known models (USD per 1M tokens, approximate). */
    private val knownPricing: Map<String, Pricing> = mapOf(
        // OpenAI
        "gpt-4o"                      to Pricing(2.50, 10.00),
        "gpt-4o-mini"                 to Pricing(0.15,  0.60),
        "gpt-4.1"                     to Pricing(2.00,  6.00),
        "o4-mini"                     to Pricing(1.10,  4.40),
        "gpt-4.1-mini"                to Pricing(0.40,  1.60),
        // Anthropic
        "claude-sonnet-4-20250514"    to Pricing(3.00, 15.00),
        "claude-opus-4-20250514"      to Pricing(15.00, 75.00),
        "claude-haiku-3.5"            to Pricing(0.80,  4.00),
        // Google
        "gemini-2.5-pro"              to Pricing(1.25,  5.00),
        "gemini-2.5-flash"            to Pricing(0.15,  0.60),
        // Mistral
        "mistral-large-latest"        to Pricing(2.00,  6.00),
        "mistral-small-latest"        to Pricing(0.20,  0.60),
        // DeepSeek
        "deepseek-chat"               to Pricing(0.27,  1.10),
        "deepseek-reasoner"           to Pricing(0.55,  2.19),
        // Meta (via Groq / Together)
        "llama-3.3-70b-versatile"     to Pricing(0.59,  0.79),
        "llama-4-maverick"            to Pricing(0.20,  0.60),
        // Qwen
        "qwen-plus"                   to Pricing(0.40,  1.20),
        // OpenRouter wildcard
        "anthropic/claude-sonnet-4"   to Pricing(3.00, 15.00),
        "openai/gpt-4o"               to Pricing(2.50, 10.00),
    )

    private val _totals = MutableStateFlow(Totals.ZERO)
    val totals: StateFlow<Totals> = _totals.asStateFlow()

    private val _sessionCallCount = MutableStateFlow(0L)
    val sessionCallCount: StateFlow<Long> = _sessionCallCount.asStateFlow()

    /** Per-call history for detailed analysis. */
    private val _callHistory = MutableStateFlow<List<CallRecord>>(emptyList())
    val callHistory: StateFlow<List<CallRecord>> = _callHistory.asStateFlow()

    data class CallRecord(
        val timestamp: Long = System.currentTimeMillis(),
        val model: String,
        val inputTokens: Long,
        val outputTokens: Long,
        val costUsd: Double
    )

    /** Override pricing or add custom model prices. */
    private val customPricing = mutableMapOf<String, Pricing>()

    fun setCustomRates(model: String, inputPerMillion: Double, outputPerMillion: Double) {
        customPricing[model] = Pricing(inputPerMillion, outputPerMillion)
    }

    // -----------------------------------------------------------------
    // RECORDING
    // -----------------------------------------------------------------

    /**
     * Record token usage from an LLM API response.
     *
     * @param model   The model identifier string (e.g. "gpt-4o-mini").
     * @param inputTokens  Prompt tokens consumed.
     * @param outputTokens Completion tokens generated.
     */
    fun record(model: String, inputTokens: Long, outputTokens: Long) {
        val pricing = pricingFor(model)
        val cost = computeCost(inputTokens, outputTokens, pricing)

        _totals.update { current ->
            current.copy(
                inputTokens = current.inputTokens + inputTokens,
                outputTokens = current.outputTokens + outputTokens,
                estimatedCostUsd = current.estimatedCostUsd + cost
            )
        }

        _sessionCallCount.update { it + 1 }

        _callHistory.update { history ->
            // Keep last 200 records to bound memory
            (history + CallRecord(
                model = model,
                inputTokens = inputTokens,
                outputTokens = outputTokens,
                costUsd = cost
            )).takeLast(200)
        }
    }

    /**
     * Estimate tokens from raw text using a simple heuristic:
     * ~4 characters per token for English text, ~2 for code-heavy text.
     * This is approximate — real token counts come from the API response.
     */
    fun estimateTokens(text: String, isCode: Boolean = false): Long {
        val charsPerToken = if (isCode) 2.5 else 4.0
        return (text.length / charsPerToken).toLong().coerceAtLeast(1)
    }

    /**
     * Record using estimated token counts when API doesn't return usage data.
     */
    fun recordEstimated(
        model: String,
        inputText: String,
        outputText: String
    ) {
        record(
            model = model,
            inputTokens = estimateTokens(inputText),
            outputTokens = estimateTokens(outputText)
        )
    }

    // -----------------------------------------------------------------
    // COST CALCULATION
    // -----------------------------------------------------------------

    private fun pricingFor(model: String): Pricing {
        customPricing[model]?.let { return it }
        // Try exact match
        knownPricing[model]?.let { return it }
        // Try substring match (e.g. "gpt-4o-mini-2025-01-01" matches "gpt-4o-mini")
        for ((key, price) in knownPricing) {
            if (model.contains(key, ignoreCase = true)) return price
        }
        // Default fallback: assume mid-tier pricing
        return Pricing(1.00, 4.00)
    }

    private fun computeCost(inputTokens: Long, outputTokens: Long, pricing: Pricing): Double {
        val inputCost  = (inputTokens.toDouble()  / 1_000_000.0) * pricing.inputPerMillion
        val outputCost = (outputTokens.toDouble() / 1_000_000.0) * pricing.outputPerMillion
        return inputCost + outputCost
    }

    // -----------------------------------------------------------------
    // FORMATTING
    // -----------------------------------------------------------------

    /** Human-readable total with unit suffix. */
    fun formatTokens(count: Long): String = when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000     -> String.format("%.1fK", count / 1_000.0)
        else               -> count.toString()
    }

    /** Formatted cost string. */
    fun formatCost(usd: Double): String {
        return if (usd < 0.01) "< \$0.01"
        else String.format("\$%.2f", usd)
    }

    /** One-line session summary for UI. */
    fun summary(): String {
        val t = _totals.value
        return "${formatTokens(t.totalTokens)} tokens · ${formatCost(t.estimatedCostUsd)} · " +
                "${_sessionCallCount.value} calls"
    }

    /** Detailed summary including per-model breakdown. */
    fun detailedSummary(): String {
        val t = _totals.value
        val byModel = _callHistory.value
            .groupBy { it.model }
            .mapValues { (_, records) ->
                records.fold(Totals.ZERO) { acc, r ->
                    acc.copy(
                        inputTokens = acc.inputTokens + r.inputTokens,
                        outputTokens = acc.outputTokens + r.outputTokens,
                        estimatedCostUsd = acc.estimatedCostUsd + r.costUsd
                    )
                }
            }

        return buildString {
            appendLine("=== Token Tracker Session Summary ===")
            appendLine("Total input:  ${formatTokens(t.inputTokens)}")
            appendLine("Total output: ${formatTokens(t.outputTokens)}")
            appendLine("Total tokens: ${formatTokens(t.totalTokens)}")
            appendLine("Total cost:   ${formatCost(t.estimatedCostUsd)}")
            appendLine("Total calls:  ${_sessionCallCount.value}")
            appendLine()
            appendLine("Per-model breakdown:")
            byModel.forEach { (model, modelTotal) ->
                appendLine("  $model")
                appendLine("    tokens: ${formatTokens(modelTotal.totalTokens)}")
                appendLine("    cost:   ${formatCost(modelTotal.estimatedCostUsd)}")
            }
        }
    }

    // -----------------------------------------------------------------
    // SESSION MANAGEMENT
    // -----------------------------------------------------------------

    /** Reset all counters for a new session. */
    fun reset() {
        _totals.value = Totals.ZERO
        _sessionCallCount.value = 0
        _callHistory.value = emptyList()
    }

    /** Snapshot current totals for comparison later. */
    fun snapshot(): Totals = _totals.value

    /** Delta since a previous snapshot. */
    fun delta(since: Totals): Totals {
        val current = _totals.value
        return Totals(
            inputTokens = current.inputTokens - since.inputTokens,
            outputTokens = current.outputTokens - since.outputTokens,
            estimatedCostUsd = current.estimatedCostUsd - since.estimatedCostUsd
        )
    }
}