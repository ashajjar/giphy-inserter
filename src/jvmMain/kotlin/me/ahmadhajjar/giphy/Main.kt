package me.ahmadhajjar.giphy

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*


@Composable
@Preview
fun App() {
    var searchTerm by remember { mutableStateOf(TextFieldValue()) }
    val focusRequester by remember { mutableStateOf(FocusRequester()) }

    MaterialTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    singleLine = true,
                    value = searchTerm,
                    onValueChange = {
                        searchTerm = it
                    },
                    placeholder = {
                        Text(text = "enter search term ...", color = Color.Gray)
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.White
                    ),
                    modifier = Modifier.focusRequester(focusRequester),
                )
                Button(onClick = ::handleSearchEvent) {
                    Text("Search")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

fun handleSearchEvent() {

}

fun main() = application {
    val windowState = rememberWindowState(
        position = WindowPosition(Alignment.Center),
        size = DpSize(500.dp, 100.dp),
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "Giphy Inserter",
        state = windowState,
        undecorated = true,
        transparent = true
    ) {
        App()
    }
}
