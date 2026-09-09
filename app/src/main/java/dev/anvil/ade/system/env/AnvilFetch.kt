package dev.anvil.ade.system.env

import android.content.Context
import java.io.File

/**
 * Neofetch-style welcome banner for the terminal - mirrors the anvil-fetch
 * shell script bundled in assets, but rendered natively so it works before
 * the embedded environment is installed. Vercel/Linear styling: flat dark
 * canvas, mono type, single accent, no decoration.
 */
object AnvilFetch {

    fun render(context: android.content.Context): String {
        val versionName = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (t: Throwable) { "0.1.0" }
        val androidVer = android.os.Build.VERSION.RELEASE
        val device = android.os.Build.MODEL
        val arch = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "aarch64"
        val envReady = EmbeddedEnvironment.isInstalled(context)

        val logo = listOf(
            "        __",
            "       /  |___________",
            "      /  /|           /|",
            "     /  / |__________/ |",
            "    /__/______________/",
            "       |   |     |   |",
            "       |___|_____|___|",
            "      /_______________\\",
            "     ~ forged on device ~"
        )

        val info = listOf(
            "anvil@android",
            "-------------",
            "OS: Anvil ADE on Android $androidVer ($arch)",
            "Host: $device",
            "Version: $versionName",
            "Shell: " + if (envReady) "embedded bootstrap (bash)" else "system sh",
            "Build: aapt2 > ecj > d8 > apksig (in-process)",
            "anvil.dev"
        )

        val sb = StringBuilder()
        val width = logo.maxOf { it.length } + 3
        for (i in 0 until maxOf(logo.size, info.size)) {
            val l = logo.getOrElse(i) { "" }.padEnd(width)
            val r = info.getOrElse(i) { "" }
            sb.append(l).append(r).append("\n")
        }
        return sb.toString()
    }
}
