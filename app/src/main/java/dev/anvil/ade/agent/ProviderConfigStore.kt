package dev.anvil.ade.agent

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Persists provider config + API keys using EncryptedSharedPreferences
 * (AES256 master key in Android Keystore). Keys are never logged and
 * never included in LLM prompts.
 */
class SecureProviderStorageUnavailableException(cause: Throwable) :
    Exception("Secure storage unavailable on this device: ${cause.message}", cause)

object ProviderConfigStore {

    private const val PREFS = "anvil_providers"
    private const val KEY_PROVIDER = "provider"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_MODEL = "model"
    private const val KEY_API_KEY = "api_key"

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
        // Do NOT silently fall back to plaintext - throw so the UI can tell
        // the user explicitly instead of storing the API key unencrypted.
        throw SecureProviderStorageUnavailableException(t)
    }

    fun load(context: Context): ProviderConfig {
        val p = prefs(context)
        val provider = LlmProvider.byId(p.getString(KEY_PROVIDER, null) ?: LlmProvider.CLAUDE.id)
        return ProviderConfig(
            provider = provider,
            baseUrl = p.getString(KEY_BASE_URL, null) ?: provider.defaultBaseUrl,
            model = p.getString(KEY_MODEL, null) ?: provider.defaultModel,
            apiKey = p.getString(KEY_API_KEY, null) ?: ""
        )
    }

    fun save(context: Context, config: ProviderConfig) {
        prefs(context).edit()
            .putString(KEY_PROVIDER, config.provider.id)
            .putString(KEY_BASE_URL, config.baseUrl)
            .putString(KEY_MODEL, config.model)
            .putString(KEY_API_KEY, config.apiKey)
            .apply()
    }
}
