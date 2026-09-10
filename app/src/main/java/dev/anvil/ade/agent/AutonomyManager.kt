package dev.anvil.ade.agent

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Per-command-type autonomy dial that gates confirmation prompts.
 *
 * Three levels per command type:
 * - [Level.ASK_ALWAYS] — every invocation requires explicit user confirmation.
 * - [Level.ASK_ONCE_PER_SESSION] — ask once per agent session, then auto-approve
 *   subsequent uses of the same command type until the session resets.
 * - [Level.AUTO_APPROVE_SAFE] — never block; the operation runs immediately.
 *
 * **Command types** map directly to the existing confirmation gates in
 * [AgentSession]: commit, terminal, file_write (write_file), memory, skill,
 * soul. Each type has its own dial independent of the others.
 *
 * The manager persists user choices via EncryptedSharedPreferences so
 * they survive app restarts.
 *
 * Integration point: call [AutonomyManager.shouldAsk] before showing a
 * confirmation dialog. If it returns false, execute the action directly.
 * After the first manual approval in a session, call [AutonomyManager.markApproved]
 * so [Level.ASK_ONCE_PER_SESSION] knows the session gate has been passed.
 */
class AutonomyManager(context: Context) {

    enum class Level {
        ASK_ALWAYS,
        ASK_ONCE_PER_SESSION,
        AUTO_APPROVE_SAFE
    }

    /**
     * Command types that map to existing agent confirmation gates.
     */
    enum class CommandType(val prefKey: String, val displayName: String) {
        COMMIT("commit", "Git Commit"),
        TERMINAL("terminal", "Terminal Command"),
        FILE_WRITE("file_write", "File Write"),
        MEMORY("memory", "Memory Update"),
        SKILL("skill", "Skill Update"),
        SOUL("soul", "Soul Update")
    }

    companion object {
        private const val PREFS_NAME = "anvil_autonomy"
        private const val KEY_PREFIX = "autonomy_level_"

        /** In-memory defaults — persisted values override these. */
        private val DEFAULTS: Map<CommandType, Level> = mapOf(
            CommandType.COMMIT to Level.ASK_ALWAYS,
            CommandType.TERMINAL to Level.ASK_ONCE_PER_SESSION,
            CommandType.FILE_WRITE to Level.AUTO_APPROVE_SAFE,
            CommandType.MEMORY to Level.ASK_ONCE_PER_SESSION,
            CommandType.SKILL to Level.ASK_ALWAYS,
            CommandType.SOUL to Level.ASK_ALWAYS
        )

        /** "Master switch" — when false, all types behave as ASK_ALWAYS. */
        private const val KEY_MASTER_ENABLED = "autonomy_master_enabled"

        /** Global default: true (autonomy dials are active). */
        private const val DEFAULT_MASTER_ENABLED = true
    }

    // ---- runtime state ----

    private val prefs: SharedPreferences

    /** Tracks which command types have been approved at least once this session. */
    private val sessionApproved = mutableSetOf<CommandType>()

    /** Observable levels so the UI can react to changes. */
    private val _levels = MutableStateFlow(mapLevels())
    val levels: StateFlow<Map<CommandType, Level>> = _levels.asStateFlow()

    /** Observable master toggle. */
    private val _masterEnabled = MutableStateFlow(DEFAULT_MASTER_ENABLED)
    val masterEnabled: StateFlow<Boolean> = _masterEnabled.asStateFlow()

    init {
        prefs = try {
            val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
            val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
            EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback: plain SharedPreferences when keystore is unavailable
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        _masterEnabled.value = prefs.getBoolean(KEY_MASTER_ENABLED, DEFAULT_MASTER_ENABLED)
    }

    // ---- public API ----

    /**
     * Returns true when a confirmation dialog SHOULD be shown for the
     * given [type]. Returns false when the action can proceed immediately.
     *
     * Logic:
     * - Master disabled → always true (ask always).
     * - Level ASK_ALWAYS → true.
     * - Level ASK_ONCE_PER_SESSION → true if not yet approved this session.
     * - Level AUTO_APPROVE_SAFE → false.
     */
    fun shouldAsk(type: CommandType): Boolean {
        if (!_masterEnabled.value) return true

        val level = getLevel(type)
        return when (level) {
            Level.ASK_ALWAYS -> true
            Level.ASK_ONCE_PER_SESSION -> type !in sessionApproved
            Level.AUTO_APPROVE_SAFE -> false
        }
    }

    /**
     * Mark a command type as having been explicitly approved by the user
     * during the current session. Only meaningful when the level is
     * [Level.ASK_ONCE_PER_SESSION].
     */
    fun markApproved(type: CommandType) {
        sessionApproved += type
    }

    /**
     * Reset all session-level approvals. Call when the agent session
     * restarts or the user starts a new conversation.
     */
    fun resetSession() {
        sessionApproved.clear()
    }

    /** Get the current autonomy level for a command type. */
    fun getLevel(type: CommandType): Level {
        val stored = prefs.getString(KEY_PREFIX + type.prefKey, null)
        return if (stored != null) {
            try { Level.valueOf(stored) } catch (_: IllegalArgumentException) { DEFAULTS[type]!! }
        } else {
            DEFAULTS[type]!!
        }
    }

    /** Persist a new autonomy level for a command type. */
    fun setLevel(type: CommandType, level: Level) {
        prefs.edit().putString(KEY_PREFIX + type.prefKey, level.name).apply()
        _levels.value = mapLevels()
    }

    /** Toggle the master autonomy switch. */
    fun setMasterEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MASTER_ENABLED, enabled).apply()
        _masterEnabled.value = enabled
    }

    /** Reset all levels to their defaults. */
    fun resetToDefaults() {
        prefs.edit().clear().apply()
        sessionApproved.clear()
        _levels.value = mapLevels()
        _masterEnabled.value = DEFAULT_MASTER_ENABLED
    }

    // ---- internal helpers ----

    private fun mapLevels(): Map<CommandType, Level> =
        CommandType.entries.associateWith { getLevel(it) }
}