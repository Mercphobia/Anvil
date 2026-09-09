package dev.anvil.ade.editor

import android.content.Context
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.DefaultGrammarDefinition
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import org.eclipse.tm4e.core.registry.IGrammarSource

/**
 * TextMate-based syntax highlighting for the sora editor.
 *
 * Grammar plist/JSON files live in assets/textmate/ and are registered once
 * per process with the GrammarRegistry singleton. Languages are created via
 * scope name ("source.java", "text.xml", ...); unknown extensions fall back
 * to null so the editor keeps plain text.
 *
 * Highlight colors come from the Quiet Light TextMate theme bundled in
 * assets (light, readable, and not tied to the app Material theme); base
 * editor colors (background, gutter, line numbers) still follow the
 * Material 3 dynamic scheme applied in SoraEditorWrapper.
 */
object TextMateLanguages {

    private const val ASSET_ROOT = "textmate"

    /** File extension (lowercase, no dot) -> TextMate scope name. */
    private val extensionScopes = mapOf(
        "java" to "source.java",
        "kt" to "source.kotlin",
        "kts" to "source.kotlin",
        "xml" to "text.xml",
        "py" to "source.python",
        "json" to "source.json",
        "sh" to "source.shell",
        "bash" to "source.shell",
        "md" to "text.html.markdown",
        "markdown" to "text.html.markdown"
    )

    /** scope name -> grammar asset file (without asset root). */
    private val scopeGrammarFiles = mapOf(
        "source.java" to "java.json",
        "source.kotlin" to "kotlin.json",
        "text.xml" to "xml.json",
        "source.python" to "python.json",
        "source.json" to "json.json",
        "source.shell" to "shell.json",
        "text.html.markdown" to "markdown.json"
    )

    @Volatile
    private var initialized = false

    /** Cached TextMateLanguage instances per scope - do NOT create a new
     *  instance per call: SoraEditorWrapper checks reference equality
     *  (editorLanguage !== language), so a fresh instance re-triggers
     *  setEditorLanguage on EVERY recomposition (every keystroke).
     *  Null values are cached too - a failed create is not retried per call. */
    private val languageCache = mutableMapOf<String, TextMateLanguage?>()

    /**
     * One-time registration of the assets file resolver and all grammars.
     * Safe to call repeatedly; failures leave [initialized] false so a later
     * call can retry.
     */
    @Synchronized
    fun ensureInitialized(context: Context) {
        if (initialized) return
        try {
            FileProviderRegistry.getInstance()
                .addFileProvider(AssetsFileResolver(context.assets))

            val registry = GrammarRegistry.getInstance()
            val definitions = scopeGrammarFiles.map { (scope, file) ->
                DefaultGrammarDefinition.withGrammarSource(
                    IGrammarSource.fromInputStream(
                        context.assets.open("$ASSET_ROOT/$file"), file, null
                    ),
                    file.removeSuffix(".json"),
                    scope
                )
            }
            registry.loadGrammars(definitions)
            initialized = true
        } catch (t: Throwable) {
            // highlighting is best-effort; plain text remains as fallback
        }
    }

    /**
     * Create a TextMate language for [fileName] based on its extension,
     * or null when the extension has no registered grammar (plain text).
     * Auto-completion identifiers are collected for smarter completion.
     */
    fun languageFor(context: Context, fileName: String?): TextMateLanguage? {
        if (fileName == null) return null
        val ext = fileName.substringAfterLast('.', "").lowercase()
        val scope = extensionScopes[ext] ?: return null
        ensureInitialized(context)
        if (!initialized) return null
        return languageCache.getOrPut(scope) {
            try {
                TextMateLanguage.create(scope, true)
            } catch (t: Throwable) {
                null
            }
        }
    }
}
