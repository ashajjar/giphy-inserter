package me.ahmadhajjar.giphy.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import me.ahmadhajjar.giphy.service.Giphy
import androidx.compose.ui.window.WindowScope
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@Composable
@Preview
fun WindowScope.App(onExit: () -> Unit) {
    val searchTerm = remember {
        val initial = listOf("Hello", "Hi", "Hey", "Welcome", "Greetings").random()
        mutableStateOf(TextFieldValue(initial, selection = TextRange(0, initial.length)))
    }
    val giphy = remember { mutableStateOf(Giphy()) }
    val isLoading = remember { mutableStateOf(false) }
    val animatedGif = remember { mutableStateOf<AnimatedGif?>(null) }
    val focusRequester by remember { mutableStateOf(FocusRequester()) }
    val showCopied = remember { mutableStateOf(false) }

    LaunchedEffect(giphy.value) {
        if (giphy.value.id != null) {
            isLoading.value = true

            val mediaUrl = "https://i.giphy.com/media/${giphy.value.id}/giphy.gif"
            withContext(Dispatchers.IO) {
                try {
                    animatedGif.value = AnimatedGif.fromURL(URL(mediaUrl))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            isLoading.value = false
        }
    }

    LaunchedEffect(showCopied.value) {
        if (showCopied.value) {
            delay(2000.milliseconds)
            showCopied.value = false
        }
    }

    MaterialTheme(
        colors = darkColors(
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC6),
            background = Color.Transparent,
            surface = Color(0xFF121212)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1E1E1E).copy(alpha = 0.85f),
                elevation = 12.dp,
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.15f))
            ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                WindowDraggableArea {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color.White.copy(alpha = 0.03f)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            "✨ Giphy Inserter",
                            style = MaterialTheme.typography.subtitle2,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp)
                        )
                        IconButton(onClick = onExit, modifier = Modifier.padding(end = 8.dp)) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SearchTextField(searchTerm, giphy, isLoading, focusRequester, showCopied)
                }

                Box(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                    if (isLoading.value) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colors.primary,
                            backgroundColor = Color.Transparent
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .fillMaxWidth()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFBB86FC).copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = 8.dp,
                        backgroundColor = Color.Black,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            val gif = animatedGif.value
                            if (gif != null) {
                                AnimatedGif(gif, Modifier.fillMaxSize())
                            } else {
                                CircularProgressIndicator(color = MaterialTheme.colors.primary.copy(alpha = 0.5f))
                            }
                        }
                    }

                    if (showCopied.value) {
                        Surface(
                            color = MaterialTheme.colors.secondary.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Text(
                                "Copied to Clipboard!",
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.body2,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "⏎ Next  •  ⌘C Copy  •  ⌘R Random",
                    style = MaterialTheme.typography.overline,
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }
}

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        withContext(Dispatchers.IO) {
            try {
                if (animatedGif.value == null) {
                    animatedGif.value = AnimatedGif.fromResource("giphy.gif")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
