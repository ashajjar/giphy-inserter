package me.ahmadhajjar.giphy

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import me.ahmadhajjar.giphy.ui.App
import java.awt.Taskbar
import java.awt.Toolkit

fun main() {
    if (System.getProperty("os.name").contains("Mac")) {
        System.setProperty("apple.awt.application.name", "Giphy Inserter")
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Giphy Inserter")
        System.setProperty("apple.laf.useScreenMenuBar", "true")
        System.setProperty("apple.awt.UIElement", "false")
        try {
            val taskbar = Taskbar.getTaskbar()
            val iconUrl = Thread.currentThread().contextClassLoader.getResource("icons/icon-big.png")
            val icon = Toolkit.getDefaultToolkit().getImage(iconUrl)
            taskbar.iconImage = icon
        } catch (_: Exception) {
            // Taskbar might not be supported or icon missing
        }
    }

    application {
        val windowState = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(400.dp, 550.dp)
        )

        var isVisible by remember { mutableStateOf(true) }

        if (isVisible) {
            Window(
                onCloseRequest = { isVisible = false },
                title = "Giphy Inserter",
                state = windowState,
                undecorated = true,
                transparent = true,
                resizable = false,
                alwaysOnTop = false,
                icon = painterResource("icons/icon-big.png")
            ) {
                window.background = java.awt.Color(0, 0, 0, 0)
                App { isVisible = false }
            }
        }

        Tray(
            icon = painterResource("icons/icon-big.png"),
            tooltip = "Giphy Inserter",
            menu = {
                Item("Show/Hide", onClick = { isVisible = !isVisible })
                Separator()
                Item("Exit", onClick = ::exitApplication)
            }
        )
    }
}
