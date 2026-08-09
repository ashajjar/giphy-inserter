package me.ahmadhajjar.giphy.utils

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed

object PlatformUtils {
    val isMac: Boolean = System.getProperty("os.name").contains("Mac", ignoreCase = true)

    val shortcutModifierLabel: String = if (isMac) "Cmd" else "Ctrl"

    fun isShortcutPressed(event: KeyEvent): Boolean {
        return if (isMac) event.isMetaPressed else event.isCtrlPressed
    }
}
