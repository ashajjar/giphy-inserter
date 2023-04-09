package me.ahmadhajjar.giphy

import App
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState(
        position = WindowPosition(Alignment.Center),
        size = DpSize(360.dp, 415.dp)
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "Giphy Inserter",
        state = windowState,
        undecorated = true,
//        transparent = true, // todo make it configurable
        resizable = false,
        icon = painterResource("icons/icon.png")
    ) {
        App(windowState)
    }
}
