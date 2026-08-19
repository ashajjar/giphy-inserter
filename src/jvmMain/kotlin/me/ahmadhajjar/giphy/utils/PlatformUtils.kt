package me.ahmadhajjar.giphy.utils

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed

object PlatformUtils {
    val isMac: Boolean = System.getProperty("os.name").contains("Mac", ignoreCase = true)

    val shortcutModifierLabel: String = if (isMac) "Cmd" else "Ctrl"

    /** Compact modifier symbol suitable for inline hints (e.g. `⌘C` or `Ctrl+C`). */
    val shortcutModifierSymbol: String = if (isMac) "⌘" else "Ctrl+"

    fun isShortcutPressed(event: KeyEvent): Boolean {
        return if (isMac) event.isMetaPressed else event.isCtrlPressed
    }
}
