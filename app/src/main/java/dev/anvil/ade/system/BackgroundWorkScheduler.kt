package dev.anvil.ade.system

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.anvil.ade.agent.AgentSession
import dev.anvil.ade.agent.AgentSessionFactory
import dev.anvil.ade.agent.LlmProvider
import dev.anvil.ade.agent.ProviderConfig
import dev.anvil.ade.agent.ProviderConfigStore
import dev.anvil.ade.model.ProjectType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * WorkManager-based cron scheduler for background agent tasks.
 *
 * Three preset intervals plus a one-shot mode:
 * - hourly
 * - daily
 * - custom (caller-supplied [repeatIntervalMinutes])
 * - oneShot (fire-once ad-hoc execution)
 *
 * Each scheduled task triggers an [AgentSession.send] with the given
 * prompt, executing tools and writing results back to the workspace.
 *
 * Usage:
 * ```
 * BackgroundWorkScheduler.scheduleHourly(context, workspaceRoot, "check build health")
 * BackgroundWorkScheduler.cancelAll(context)
 * ```
 */
object BackgroundWorkScheduler {

    private const val TAG = "AnvilBgScheduler"
    private const val WORK_NAME_HOURLY = "anvil_agent_hourly"
    private const val WORK_NAME_DAILY = "anvil_agent_daily"
    private const val WORK_NAME_CUSTOM = "anvil_agent_custom"

    // ---- public API ----

    /** Schedule an agent task that runs every hour. */
    fun scheduleHourly(
        context: Context,
        workspaceRoot: File,
        prompt: String,
        projectType: ProjectType = ProjectType.ANDROID
    ) {
        schedulePeriodic(
            context, workspaceRoot, prompt, projectType,
            WORK_NAME_HOURLY, repeatIntervalMinutes = 60
        )
    }

    /** Schedule an agent task that runs once every 24 hours. */
    fun scheduleDaily(
        context: Context,
        workspaceRoot: File,
        prompt: String,
        projectType: ProjectType = ProjectType.ANDROID
    ) {
        schedulePeriodic(
            context, workspaceRoot, prompt, projectType,
            WORK_NAME_DAILY, repeatIntervalMinutes = 24 * 60
        )
    }

    /**
     * Schedule an agent task with a custom repeat interval (in minutes).
     * The minimum allowed by WorkManager is 15 minutes.
     */
    fun scheduleCustom(
        context: Context,
        workspaceRoot: File,
        prompt: String,
        projectType: ProjectType = ProjectType.ANDROID,
        repeatIntervalMinutes: Long
    ) {
        schedulePeriodic(
            context, workspaceRoot, prompt, projectType,
            WORK_NAME_CUSTOM, repeatIntervalMinutes.coerceAtLeast(15)
        )
    }

    /** Fire a one-shot agent task immediately. */
    fun scheduleOneShot(
        context: Context,
        workspaceRoot: File,
        prompt: String,
        projectType: ProjectType = ProjectType.ANDROID,
        tag: String = "anvil_oneshot"
    ) {
        val inputData = Data.Builder()
            .putString(KEY_PROMPT, prompt)
            .putString(KEY_WORKSPACE, workspaceRoot.absolutePath)
            .putString(KEY_PROJECT_TYPE, projectType.name)
            .build()

        val request = OneTimeWorkRequestBuilder<AgentWorker>()
            .setInputData(inputData)
            .setConstraints(networkConstraint(context))
            .addTag(tag)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, request)

        Log.i(TAG, "One-shot agent task queued tag=$tag prompt=$prompt")
    }

    /** Cancel all scheduled agent tasks. */
    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
        Log.i(TAG, "All background agent tasks cancelled")
    }

    /** Cancel a specific named schedule. */
    fun cancel(context: Context, workName: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName)
        Log.i(TAG, "Cancelled work: $workName")
    }

    // ---- internals ----

    private const val KEY_PROMPT = "agent_prompt"
    private const val KEY_WORKSPACE = "workspace_root"
    private const val KEY_PROJECT_TYPE = "project_type"

    private fun schedulePeriodic(
        context: Context,
        workspaceRoot: File,
        prompt: String,
        projectType: ProjectType,
        workName: String,
        repeatIntervalMinutes: Long
    ) {
        val inputData = Data.Builder()
            .putString(KEY_PROMPT, prompt)
            .putString(KEY_WORKSPACE, workspaceRoot.absolutePath)
            .putString(KEY_PROJECT_TYPE, projectType.name)
            .build()

        val request = PeriodicWorkRequestBuilder<AgentWorker>(
            repeatIntervalMinutes, TimeUnit.MINUTES
        )
            .setInputData(inputData)
            .setConstraints(networkConstraint(context))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .addTag(TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.UPDATE, request)

        Log.i(TAG, "Scheduled $workName every ${repeatIntervalMinutes}min prompt=$prompt")
    }

    /**
     * Require an unmetered network when possible, or any network as
     * fallback. If the user configured a local Ollama endpoint we skip
     * the of metered network entirely.
     */
    private fun networkConstraint(context: Context): Constraints {
        val config = runCatching {
            ProviderConfigStore.load(context)
        }.getOrNull()

        val needsNetwork = config?.provider?.let { provider ->
            provider == LlmProvider.OLLAMA || provider == LlmProvider.CUSTOM
        } != true

        return if (needsNetwork) {
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        } else {
            Constraints.Builder().build()
        }
    }

    // ---- worker ----

    /**
     * CoroutineWorker that loads the LLM config, creates an [AgentSession],
     * and fires the scheduled prompt. Results are written to the workspace
     * via normal tool execution.
     */
    class AgentWorker(
        appContext: Context,
        params: WorkerParameters
    ) : CoroutineWorker(appContext, params) {

        override suspend fun doWork(): Result {
            val prompt = inputData.getString(KEY_PROMPT)
                ?: return Result.failure(Data.Builder().putString("reason", "missing prompt").build())

            val workspacePath = inputData.getString(KEY_WORKSPACE)
                ?: return Result.failure(Data.Builder().putString("reason", "missing workspace").build())

            val projectTypeName = inputData.getString(KEY_PROJECT_TYPE)
            val projectType = runCatching {
                ProjectType.valueOf(projectTypeName ?: "ANDROID")
            }.getOrDefault(ProjectType.ANDROID)

            val workspaceRoot = File(workspacePath)
            if (!workspaceRoot.isDirectory) {
                Log.w(TAG, "Workspace not found: $workspacePath — skipping")
                return Result.retry()
            }

            val config = runCatching {
                ProviderConfigStore.load(applicationContext)
            }.getOrElse { e ->
                Log.e(TAG, "Failed to load LLM config", e)
                return Result.failure(Data.Builder().putString("reason", "config load failed").build())
            }

            return try {
                val session = AgentSessionFactory.create(
                    context = applicationContext,
                    scope = kotlinx.coroutines.MainScope(),
                    workspaceRoot = workspaceRoot,
                    projectType = projectType,
                    hooks = AgentSessionFactory.Hooks(
                        onSteps = { /* silent — no UI for background tasks */ },
                        onBusy = { /* no-op */ },
                        onPendingMemoryEntry = { /* auto-confirm? skip for now */ },
                        onPendingSkillProposal = { /* skip */ },
                        onPendingTerminalCommand = { /* skip */ },
                        onPendingSoulProposal = { /* skip */ }
                    )
                )

                session.projectType = projectType
                session.send(prompt)

                // Wait until the session is no longer busy or a timeout
                val deadline = System.currentTimeMillis() + 5 * 60 * 1000L // 5 min
                while (session.busy.value && System.currentTimeMillis() < deadline) {
                    kotlinx.coroutines.delay(500)
                    if (isStopped) throw CancellationException("Worker cancelled")
                }

                Log.i(TAG, "Background agent task completed: $prompt")
                Result.success()
            } catch (e: CancellationException) {
                Log.w(TAG, "Background agent task cancelled")
                Result.retry()
            } catch (e: Exception) {
                Log.e(TAG, "Background agent task failed", e)
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        }
    }
}