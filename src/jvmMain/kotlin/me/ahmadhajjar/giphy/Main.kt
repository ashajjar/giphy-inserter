package me.ahmadhajjar.giphy

import me.ahmadhajjar.giphy.ui.App
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*

fun main() = application {
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
            alwaysOnTop = true,
            icon = painterResource("icons/icon.png")
        ) {
            App { isVisible = false }
        }
    }

    Tray(
        icon = painterResource("icons/icon.png"),
        tooltip = "Giphy Inserter",
        menu = {
            Item("Show/Hide", onClick = { isVisible = !isVisible })
            Separator()
            Item("Exit", onClick = ::exitApplication)
        }
    )
}
