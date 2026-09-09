package dev.anvil.ade.system

/**
 * Sanitizes AOSP layout XML before mockup inflation by replacing private
 * framework resource pointers with safe fallbacks.
 */
object XmlResourceInterceptor {

    private val colorPattern = Regex("\"@\\*android:color/[a-zA-Z0-9_]+\"")
    private val dimenPattern = Regex("\"@\\*android:dimen/[a-zA-Z0-9_]+\"")
    private val drawablePattern = Regex("\"@\\*android:drawable/[a-zA-Z0-9_]+\"")
    private val stylePattern = Regex("\"@\\*android:style/[a-zA-Z0-9_.]+\"")

    fun sanitizeAospXml(rawXml: String): String {
        return rawXml
            .replace(colorPattern, "\"#808080\"")
            .replace(dimenPattern, "\"24dp\"")
            .replace(drawablePattern, "\"#404040\"")
            .replace(stylePattern, "\"@android:style/TextAppearance\"")
    }

    /** Basic well-formedness check used before preview (cheap guard). */
    fun isWellFormed(xml: String): Boolean {
        return try {
            val parser = android.util.Xml.newPullParser()
            parser.setInput(java.io.StringReader(xml))
            var event = parser.eventType
            while (event != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                event = parser.next()
            }
            true
        } catch (t: Throwable) {
            false
        }
    }
}
