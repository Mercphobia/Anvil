package com.vibe.forge.ui.components

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.vibe.forge.system.AospMockupInflater

/**
 * Hosts the AOSP mockup preview: renders raw XML through the custom
 * inflater; unknown views appear as red labeled boxes; malformed XML
 * shows an error overlay instead of crashing.
 */
@Composable
fun MockupCanvas(
    xmlContent: String,
    modifier: Modifier = Modifier
) {
    if (xmlContent.isBlank()) {
        Text(
            "No XML to preview",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
        return
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context -> FrameLayout(context) },
        update = { container ->
            container.removeAllViews()
            try {
                val view = AospMockupInflater.inflate(container.context, xmlContent)
                container.addView(view)
            } catch (t: Throwable) {
                val errorView = android.widget.TextView(container.context).apply {
                    text = "XML error: ${t.message}"
                    setTextColor(android.graphics.Color.RED)
                    setPadding(24, 24, 24, 24)
                }
                container.addView(errorView)
            }
        }
    )
}
