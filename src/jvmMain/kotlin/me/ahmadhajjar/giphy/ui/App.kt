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
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import me.ahmadhajjar.giphy.service.Giphy
import me.ahmadhajjar.giphy.ui.SearchTextField
import me.ahmadhajjar.giphy.ui.originalHeight
import me.ahmadhajjar.giphy.ui.originalWidth
import java.net.URL
import javax.swing.ImageIcon
import javax.swing.JLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

class AppId

@Composable
@Preview
fun WindowScope.App(windowState: WindowState, onExit: () -> Unit) {
    val searchTerm = remember { mutableStateOf(TextFieldValue()) }
    val giphy = remember { mutableStateOf(Giphy()) }
    val isLoading = remember { mutableStateOf(false) }
    val iconState = remember { mutableStateOf<ImageIcon?>(null) }
    val focusRequester by remember { mutableStateOf(FocusRequester()) }

    LaunchedEffect(giphy.value) {
        if (giphy.value.id != null) {
            isLoading.value = true

            if (giphy.value.icon != null) {
                iconState.value = giphy.value.icon
                isLoading.value = false
                return@LaunchedEffect
            }

            val mediaUrl = "https://i.giphy.com/media/${giphy.value.id}/giphy.gif"
            val icon = withContext(Dispatchers.IO) {
                try {
                    ImageIcon(URL(mediaUrl))
                } catch (e: Exception) {
                    null
                }
            }
            giphy.value.icon = icon
            iconState.value = icon
            isLoading.value = false
        }
    }

    MaterialTheme(
        colors = darkColors(
            primary = Color(0xFFBB86FC),
            background = Color.Transparent,
            surface = Color(0xFF121212)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E).copy(alpha = 0.95f),
            elevation = 10.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                WindowDraggableArea {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(Color.White.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            "Giphy Inserter",
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                        )
                        IconButton(onClick = onExit) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SearchTextField(searchTerm, giphy, isLoading, focusRequester)
                }

                if (isLoading.value) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = MaterialTheme.colors.primary
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SwingPanel(
                        modifier = Modifier.size(
                            giphy.value.originalWidth(360),
                            giphy.value.originalHeight(360)
                        ),
                        factory = {
                            JLabel(ImageIcon(AppId().javaClass.getResource("giphy.gif")))
                        },
                        update = {
                            windowState.size = DpSize(
                                giphy.value.originalWidth(360),
                                giphy.value.originalHeight(360) + 42.dp // Add title bar height + loading bar
                            )
                            it.icon = iconState.value ?: ImageIcon(AppId().javaClass.getResource("giphy.gif"))
                        }
                    )

                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
