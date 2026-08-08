@file:OptIn(ExperimentalComposeUiApi::class)

package me.ahmadhajjar.giphy.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ahmadhajjar.giphy.service.*
import me.ahmadhajjar.giphy.config.ConfigService
import java.awt.Toolkit
import java.awt.datatransfer.*
import java.net.URL
import kotlin.system.exitProcess

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun SearchTextField(
    searchTerm: MutableState<TextFieldValue>,
    giphy: MutableState<Giphy>,
    isLoading: MutableState<Boolean>,
    focusRequester: FocusRequester,
    showCopied: MutableState<Boolean>,
    errorMessage: MutableState<String?>
) {
    val scope = rememberCoroutineScope()

    TextField(
        trailingIcon = {
            Icon(
                painter = painterResource("powered-by-giphy.png"),
                contentDescription = "Powered By Giphy",
                tint = Color.Unspecified,
                modifier = Modifier.size(80.dp)
            )
        },
        singleLine = true,
        value = searchTerm.value,
        onValueChange = {
            searchTerm.value = it
        },
        placeholder = {
            Text(text = "Search for awesome GIFs...", color = Color.Gray.copy(alpha = 0.5f))
        },
        colors = TextFieldDefaults.textFieldColors(
            textColor = Color.White,
            backgroundColor = Color.White.copy(alpha = 0.05f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color(0xFFBB86FC)
        ),
        shape = RoundedCornerShape(12.dp),
        textStyle = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        ),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(56.dp)
            .focusRequester(focusRequester)
            .onPreviewKeyEvent {
                if (it.type != KeyEventType.KeyDown) {
                    return@onPreviewKeyEvent false
                }

                when (it.key) {
                    Key.Escape -> exitProcess(0)
                    Key.C -> {
                        if (it.isCtrlPressed || it.isMetaPressed) {
                            scope.launch(Dispatchers.IO) {
                                if (copyGifToClipboard(giphy.value)) {
                                    showCopied.value = true
                                }
                            }
                            return@onPreviewKeyEvent true
                        }
                    }

                    Key.R -> {
                        if (it.isCtrlPressed || it.isMetaPressed) {
                            if (ConfigService.apiKey.isEmpty()) {
                                errorMessage.value = "Please set your Giphy API Key in Settings (Cmd+,)"
                                return@onPreviewKeyEvent true
                            }
                            errorMessage.value = null
                            if (!isLoading.value) {
                                scope.launch(Dispatchers.IO) {
                                    isLoading.value = true
                                    val newGiphy = GiphyService.randomGiphy() ?: Giphy()
                                    giphy.value = newGiphy
                                    GiphyAnalytics.handleGiphyEvent(newGiphy, GiphyEvent.LOADED)
                                    isLoading.value = false
                                }
                            }
                            return@onPreviewKeyEvent true
                        }
                    }

                    Key.Enter, Key.DirectionDown -> {
                        if (it.isCtrlPressed || it.isMetaPressed) {
                            scope.launch(Dispatchers.IO) {
                                if (copyGifToClipboard(giphy.value)) {
                                    showCopied.value = true
                                }
                            }
                            return@onPreviewKeyEvent true
                        }

                        if (ConfigService.apiKey.isEmpty()) {
                            errorMessage.value = "Please set your Giphy API Key in Settings (Cmd+,)"
                            return@onPreviewKeyEvent true
                        }
                        errorMessage.value = null

                        if (!isLoading.value) {
                            scope.launch(Dispatchers.IO) {
                                isLoading.value = true
                                val newGiphy = GiphyService.nextGiphy(searchTerm.value.text) ?: Giphy()
                                giphy.value = newGiphy
                                GiphyAnalytics.handleGiphyEvent(newGiphy, GiphyEvent.LOADED)
                                isLoading.value = false
                            }
                        }
                        return@onPreviewKeyEvent true
                    }

                    Key.DirectionUp -> {
                        if (it.isCtrlPressed || it.isMetaPressed) {
                            scope.launch(Dispatchers.IO) {
                                if (copyGifToClipboard(giphy.value)) {
                                    showCopied.value = true
                                    insertGiphy(giphy.value)
                                }
                            }
                            return@onPreviewKeyEvent true
                        }

                        if (ConfigService.apiKey.isEmpty()) {
                            errorMessage.value = "Please set your Giphy API Key in Settings (Cmd+,)"
                            return@onPreviewKeyEvent true
                        }
                        errorMessage.value = null

                        if (!isLoading.value) {
                            scope.launch(Dispatchers.IO) {
                                isLoading.value = true
                                val newGiphy = GiphyService.previousGiphy(searchTerm.value.text) ?: Giphy()
                                giphy.value = newGiphy
                                GiphyAnalytics.handleGiphyEvent(newGiphy, GiphyEvent.LOADED)
                                isLoading.value = false
                            }
                        }
                        return@onPreviewKeyEvent true
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

    try {
        val bytes = URL(mediaUrl).readBytes()
        val selection = GiphyTransferable(mediaUrl, bytes)
        val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(selection, null)
        GiphyAnalytics.handleGiphyEvent(giphy, GiphyEvent.SENT)
        return true
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback to simple URL copy if byte copy fails
        val selection = StringSelection(mediaUrl)
        val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(selection, null)
        return true
    }
}

class GiphyTransferable(private val url: String, private val gifBytes: ByteArray) : Transferable {
    private val gifFlavor = DataFlavor("image/gif;class=java.io.InputStream", "Animated GIF")
    private val htmlFlavor = DataFlavor("text/html;class=java.lang.String", "HTML Text")

    override fun getTransferDataFlavors(): Array<DataFlavor> {
        return arrayOf(gifFlavor, DataFlavor.stringFlavor, htmlFlavor)
    }

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean {
        return transferDataFlavors.any { it.equals(flavor) }
    }

    override fun getTransferData(flavor: DataFlavor): Any {
        return when {
            flavor.equals(gifFlavor) -> java.io.ByteArrayInputStream(gifBytes)
            flavor.equals(DataFlavor.stringFlavor) -> url
            flavor.equals(htmlFlavor) -> "<img src='$url' />"
            else -> throw UnsupportedFlavorException(flavor)
        }
    }
}
