package dev.anvil.ade.agent

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Generic evaluator-optimizer retry loop.
 *
 * Pattern:
 *   1. Execute action
 *   2. Evaluate result against criteria
 *   3. If evaluation fails → generate feedback → retry (up to maxRetries)
 *   4. If maxRetries exhausted → return failure with accumulated feedback
 *
 * Used by run_build workflows, file-editing pipelines, and any
 * multi-step operation where the agent iterates to get it right.
 *
 * Thread-safe: all state mutations flow through MutableStateFlow.
 */
class EvaluatorOptimizerLoop<Input, Output>(
    val maxRetries: Int = 3,
    val name: String = "eval-opt-loop"
) {
    data class Attempt<Output>(
        val index: Int,
        val output: Output? = null,
        val evaluation: EvalResult = EvalResult.PENDING,
        val feedback: String = ""
    )

    sealed class EvalResult {
        data object PASS : EvalResult()
        data class FAIL(val reason: String) : EvalResult()
        data object PENDING : EvalResult()

        val isPass: Boolean get() = this is PASS
    }

    data class Result<Output>(
        val success: Boolean,
        val output: Output?,
        val attempts: List<Attempt<Output>>,
        val finalFeedback: String = ""
    )

    private val _attempts = MutableStateFlow<List<Attempt<Output>>>(emptyList())
    val attempts: StateFlow<List<Attempt<Output>>> = _attempts.asStateFlow()

    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()

    private val _currentAttempt = MutableStateFlow(0)
    val currentAttempt: StateFlow<Int> = _currentAttempt.asStateFlow()

    /**
     * Runs the loop.
     *
     * @param execute  Produces the output for one attempt. Receives feedback from
     *                 the previous failed evaluation (empty string on first attempt).
     * @param evaluate Judges the output. Return EvalResult.PASS on success,
     *                 EvalResult.FAIL with a reason that guides the next execute().
     */
    suspend fun run(
        execute: suspend (feedback: String) -> Output,
        evaluate: suspend (Output) -> EvalResult
    ): Result<Output> {
        _running.value = true
        _attempts.value = emptyList()
        val record = mutableListOf<Attempt<Output>>()
        var feedback = ""
        var lastOutput: Output? = null

        for (i in 0..maxRetries) {
            _currentAttempt.value = i
            val output = try {
                execute(feedback)
            } catch (e: Exception) {
                val fail = EvalResult.FAIL("Exception: ${e.message}")
                val attempt = Attempt(i, null, fail, "execute threw: ${e.message}")
                record += attempt
                _attempts.value = record.toList()
                feedback = fail.reason
                continue
            }

            lastOutput = output
            val eval = try {
                evaluate(output)
            } catch (e: Exception) {
                EvalResult.FAIL("Evaluator exception: ${e.message}")
            }

            val attempt = Attempt(i, output, eval, feedback)
            record += attempt
            _attempts.value = record.toList()

            if (eval.isPass) {
                _running.value = false
                return Result(success = true, output = output, attempts = record)
            }

            feedback = (eval as EvalResult.FAIL).reason
        }

        _running.value = false
        return Result(
            success = false,
            output = lastOutput,
            attempts = record,
            finalFeedback = feedback
        )
    }

    /** Convenience: run with a simple predicate evaluator. */
    suspend fun runWithPredicate(
        execute: suspend (feedback: String) -> Output,
        predicate: (Output) -> Boolean,
        failReason: (Output) -> String = { "output did not satisfy predicate" }
    ): Result<Output> = run(
        execute = execute,
        evaluate = { out ->
            if (predicate(out)) EvalResult.PASS
            else EvalResult.FAIL(failReason(out))
        }
    )

    /** Reset internal state (call before reusing the instance). */
    fun reset() {
        _attempts.value = emptyList()
        _currentAttempt.value = 0
        _running.value = false
    }
}