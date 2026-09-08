package com.vibe.forge.system.env

/**
 * Neofetch-style welcome banner shown when the terminal opens.
 * The ASCII art mirrors the app launcher logo (the "V" with forge spark).
 */
object ForgeBanner {

    fun render(context: android.content.Context): String {
        val versionName = try {
            context.packageManager
                .getPackageInfo(context.packageName, 0).versionName
        } catch (t: Throwable) {
            "0.1.0"
        }
        val androidVer = android.os.Build.VERSION.RELEASE
        val device = android.os.Build.MODEL
        val envReady = EmbeddedEnvironment.isInstalled(context)

        val logo = listOf(
            "\\        /",
            " \\      / ",
            "  \\    /  ",
            "   \\  /   ",
            "    \\/\/  ",
            "    /\\/\\ ",
            "   /  \\  \\",
            "  -------- "
        )

        val info = listOf(
            "Vibe Forge",
            "---------",
            "Version: $versionName",
            "Android: $androidVer",
            "Device: $device",
            "Shell: " + if (envReady) "bootstrap (embedded)" else "system sh",
            "Workspace: " + java.io.File(context.filesDir, "workspace").absolutePath,
            "Type 'help' for commands"
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
