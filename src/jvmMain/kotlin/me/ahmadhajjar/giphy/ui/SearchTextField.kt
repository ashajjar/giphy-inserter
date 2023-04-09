@file:OptIn(ExperimentalComposeUiApi::class)

package me.ahmadhajjar.giphy.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import me.ahmadhajjar.giphy.service.Giphy
import me.ahmadhajjar.giphy.service.GiphyAnalytics
import me.ahmadhajjar.giphy.service.GiphyEvent
import me.ahmadhajjar.giphy.service.GiphyService
import me.ahmadhajjar.giphy.utils.BasicTransferable
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import kotlin.system.exitProcess

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun SearchTextField(
    searchTerm: MutableState<TextFieldValue>,
    giphy: MutableState<Giphy>,
    focusRequester: FocusRequester
) {
    var fieldWidth = giphy.value.originalWidth(500)
    if (fieldWidth.value < 500) {
        fieldWidth = 500.dp
    }
    TextField(
        trailingIcon = {
            Icon(
                painter = painterResource("powered-by-giphy.png"),
                contentDescription = "Powered By Giphy",
                tint = Color.Unspecified
            )
        },
        singleLine = true,
        value = searchTerm.value,
        onValueChange = {
            searchTerm.value = it
        },
        placeholder = {
            Text(text = "Start searching ...", color = Color.Gray)
        },
        colors = TextFieldDefaults.textFieldColors(
            textColor = Color.hsl(275f, 0.44f, 0.47f),
            backgroundColor = Color.hsl(240F, 0.13F, 0.13F)
        ),
        textStyle = TextStyle(
            fontWeight = FontWeight.Bold,
        ),
        modifier = Modifier
            .size(
                fieldWidth,
                60.dp
            )
            .focusRequester(focusRequester)
            .onPreviewKeyEvent {
                if (it.type != KeyEventType.KeyDown) {
                    return@onPreviewKeyEvent false
                }

                when (it.key) {
                    Key.Escape -> exitProcess(0)
                    Key.C -> {
                        if ((it.isCtrlPressed || it.isMetaPressed) && copyGifToClipboard(giphy.value)) {
                            return@onPreviewKeyEvent false
                        }
                    }

                    Key.DirectionDown, Key.Enter -> {
                        if ((it.isCtrlPressed || it.isMetaPressed) && copyGifToClipboard(giphy.value)) {
                            return@onPreviewKeyEvent false
                        }

                        if (searchTerm.value.text.trim().length < 2) {
                            return@onPreviewKeyEvent false
                        }
                        giphy.value = GiphyService.nextGiphy(searchTerm.value.text) ?: Giphy()
                        GiphyAnalytics.handleGiphyEvent(giphy.value, GiphyEvent.LOADED)
                    }

                    Key.DirectionUp -> {
                        if ((it.isCtrlPressed || it.isMetaPressed) && copyGifToClipboard(giphy.value)) {
                            insertGiphy(giphy.value)
                            return@onPreviewKeyEvent false
                        }

                        if (searchTerm.value.text.trim().length < 2) {
                            return@onPreviewKeyEvent false
                        }
                        giphy.value = GiphyService.previousGiphy(searchTerm.value.text) ?: Giphy()
                        GiphyAnalytics.handleGiphyEvent(giphy.value, GiphyEvent.LOADED)
                    }
                }

                false
            },
    )
}

fun insertGiphy(giphy: Giphy?) {

}

fun copyGifToClipboard(giphy: Giphy?): Boolean {
    if (giphy?.id == null) {
        return false
    }

    val mediaUrl = "https://i.giphy.com/media/${giphy.id}/giphy.gif"
    val selection = BasicTransferable(mediaUrl, "<img src='$mediaUrl'/>")
    val textSelection = StringSelection(mediaUrl)
    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard

    clipboard.setContents(selection, textSelection)
    GiphyAnalytics.handleGiphyEvent(giphy, GiphyEvent.SENT)
    return true
}
