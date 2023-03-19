@file:OptIn(ExperimentalComposeUiApi::class)

package me.ahmadhajjar.giphy.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import me.ahmadhajjar.giphy.service.Giphy
import me.ahmadhajjar.giphy.service.GiphyService
import kotlin.system.exitProcess

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun SearchTextField(
    searchTerm: MutableState<TextFieldValue>,
    giphyService: MutableState<GiphyService>,
    giphy: MutableState<Giphy?>,
    focusRequester: FocusRequester
) {
    var fieldWidth = giphy.value?.images?.original?.width?.toInt() ?: 0
    if (fieldWidth < 500) {
        fieldWidth = 500
    }
    TextField(
        singleLine = true,
        value = searchTerm.value,
        onValueChange = {
            searchTerm.value = it
        },
        placeholder = {
            Text(text = "enter search term ...", color = Color.Gray)
        },
        colors = TextFieldDefaults.textFieldColors(
            textColor = Color.White
        ),
        modifier = Modifier
            .size(
                fieldWidth.dp,
                60.dp
            )
            .focusRequester(focusRequester)
            .onPreviewKeyEvent {
                if (it.type != KeyEventType.KeyDown) {
                    return@onPreviewKeyEvent false
                }

                when (it.key) {
                    Key.Escape -> exitProcess(0)
                    Key.Enter -> {
                        if (searchTerm.value.text.length < 3) {
                            return@onPreviewKeyEvent false
                        }
                        giphy.value = giphyService.value.nextGiphy(searchTerm.value.text)
                    }
                }

                false
            },
    )
}
