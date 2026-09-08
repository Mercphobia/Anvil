package com.vibe.forge.system

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Xml
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

/**
 * Inflates raw AOSP layout XML strings without compiled resources.
 * Unknown system views are replaced with red labeled placeholders.
 */
object AospMockupInflater {

    fun inflate(context: Context, rawXml: String): View {
        val sanitized = XmlResourceInterceptor.sanitizeAospXml(rawXml)
        val parser: XmlPullParser = Xml.newPullParser().apply {
            setInput(StringReader(sanitized))
        }
        val inflater = LayoutInflater.from(context).cloneInContext(context)
        inflater.factory2 = MockFactory(inflater)
        return inflater.inflate(parser, null)
    }

    private class MockFactory(
        private val inflater: LayoutInflater
    ) : LayoutInflater.Factory2 {

        override fun onCreateView(
            parent: View?, name: String, context: Context, attrs: AttributeSet
        ): View? = createSafe(name, context, attrs)

        override fun onCreateView(
            name: String, context: Context, attrs: AttributeSet
        ): View? = createSafe(name, context, attrs)

        private fun createSafe(name: String, context: Context, attrs: AttributeSet): View? {
            return try {
                var view: View? = null
                for (prefix in arrayOf("android.widget.", "android.view.", "android.app.")) {
                    try {
                        view = inflater.createView(name, prefix, attrs)
                        if (view != null) break
                    } catch (ignored: ClassNotFoundException) {
                    }
                }
                view ?: mockView(context, name)
            } catch (e: ClassNotFoundException) {
                mockView(context, name)
            } catch (e: InflateException) {
                mockView(context, name)
            } catch (t: Throwable) {
                mockView(context, name)
            }
        }

        private fun mockView(context: Context, tagName: String): View {
            val container = FrameLayout(context).apply {
                setBackgroundColor(0x33FF0000)
                minimumHeight = 64
                minimumWidth = 64
            }
            val label = TextView(context).apply {
                text = tagName.substringAfterLast('.')
                setTextColor(Color.RED)
                textSize = 10f
            }
            container.addView(label)
            return container
        }
    }
}
