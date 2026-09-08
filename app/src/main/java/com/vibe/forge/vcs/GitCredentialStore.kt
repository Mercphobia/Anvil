package com.vibe.forge.vcs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores the Git token encrypted (Android Keystore). Never logged,
 * never included in LLM prompts.
 */
object GitCredentialStore {

    private const val PREFS = "vibe_forge_git"
    private const val KEY_TOKEN = "git_token"
    private const val KEY_REMOTE = "git_remote"
    private const val KEY_BRANCH = "git_branch"

    private fun prefs(context: Context) = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (t: Throwable) {
        context.getSharedPreferences(PREFS + "_plain", Context.MODE_PRIVATE)
    }

    fun save(context: Context, token: String, remote: String, branch: String) {
        prefs(context).edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_REMOTE, remote)
            .putString(KEY_BRANCH, branch)
            .apply()
    }

    fun token(context: Context): String = prefs(context).getString(KEY_TOKEN, null) ?: ""
    fun remote(context: Context): String = prefs(context).getString(KEY_REMOTE, null) ?: ""
    fun branch(context: Context): String =
        prefs(context).getString(KEY_BRANCH, null) ?: "ai-mockup/default"

    fun isConfigured(context: Context): Boolean = token(context).isNotBlank()
}
