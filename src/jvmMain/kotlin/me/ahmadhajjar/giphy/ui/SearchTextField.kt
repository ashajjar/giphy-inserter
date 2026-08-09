@file:OptIn(ExperimentalComposeUiApi::class)

package me.ahmadhajjar.giphy.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ahmadhajjar.giphy.config.ConfigService
import me.ahmadhajjar.giphy.service.Giphy
import me.ahmadhajjar.giphy.service.GiphyAnalytics
import me.ahmadhajjar.giphy.service.GiphyEvent
import me.ahmadhajjar.giphy.service.GiphyService
import me.ahmadhajjar.giphy.utils.ClipboardUtil
import me.ahmadhajjar.giphy.utils.PlatformUtils
import java.io.File
import java.net.URL

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun SearchTextField(
    searchTerm: MutableState<TextFieldValue>,
    giphy: MutableState<Giphy>,
    isLoading: MutableState<Boolean>,
    focusRequester: FocusRequester,
    showCopied: MutableState<Boolean>,
    errorMessage: MutableState<String?>,
    onExit: () -> Unit
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
            .onFocusChanged {
                if (it.isFocused) {
                    searchTerm.value = searchTerm.value.copy(selection = TextRange(searchTerm.value.text.length))
                }
            }
            .onPreviewKeyEvent {
                if (it.type != KeyEventType.KeyDown) {
                    return@onPreviewKeyEvent false
                }

                when (it.key) {
                    Key.Escape -> onExit()
                    Key.C -> {
                        if (PlatformUtils.isShortcutPressed(it)) {
                            scope.launch(Dispatchers.IO) {
                                if (copyGifToClipboard(giphy.value)) {
                                    showCopied.value = true
                                }
                            }
                            return@onPreviewKeyEvent true
                        }
                    }

                    Key.R -> {
                        if (PlatformUtils.isShortcutPressed(it)) {
                            if (ConfigService.apiKey.isEmpty()) {
                                errorMessage.value = "Please set your Giphy API Key in Settings (${PlatformUtils.shortcutModifierLabel}+,)"
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
                        if (PlatformUtils.isShortcutPressed(it)) {
                            scope.launch(Dispatchers.IO) {
                                if (copyGifToClipboard(giphy.value)) {
                                    showCopied.value = true
                                }
                            }
                            return@onPreviewKeyEvent true
                        }

                        if (ConfigService.apiKey.isEmpty()) {
                            errorMessage.value = "Please set your Giphy API Key in Settings (${PlatformUtils.shortcutModifierLabel}+,)"
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
                        if (PlatformUtils.isShortcutPressed(it)) {
                            scope.launch(Dispatchers.IO) {
                                if (copyGifToClipboard(giphy.value)) {
                                    showCopied.value = true
                                    insertGiphy(giphy.value)
                                }
                            }
                            return@onPreviewKeyEvent true
                        }

                        if (ConfigService.apiKey.isEmpty()) {
                            errorMessage.value = "Please set your Giphy API Key in Settings (${PlatformUtils.shortcutModifierLabel}+,)"
                            return@onPreviewKeyEvent true
                        }
                        errorMessage.value = null

                        if (!isLoading.value) {
                            scope.launch(Dispatchers.IO) {
                                isLoading.value = true
                                val newGiphy = GiphyService.previousGiphy() ?: Giphy()
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
    // todo later implement insert giphy
    println("Inserting Giphy: ${giphy?.id}")
}

fun copyGifToClipboard(giphy: Giphy?): Boolean {
    if (giphy?.id == null) {
        return false
    }

    val mediaUrl = "https://i.giphy.com/media/${giphy.id}/giphy.gif"

    return try {
        val bytes = URL(mediaUrl).readBytes()
        // Keep a stable .gif name so paste targets treat this as GIF media.
        val tempFile = File.createTempFile("giphy-${giphy.id}-", ".gif")
        tempFile.writeBytes(bytes)
        tempFile.deleteOnExit()

        val copied = ClipboardUtil.copyAnimatedGif(tempFile, bytes)
        if (copied) {
            GiphyAnalytics.handleGiphyEvent(giphy, GiphyEvent.SENT)
        }
        copied
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
