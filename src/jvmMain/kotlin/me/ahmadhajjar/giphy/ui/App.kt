import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import me.ahmadhajjar.giphy.service.Giphy
import me.ahmadhajjar.giphy.service.GiphyService
import me.ahmadhajjar.giphy.ui.SearchTextField
import me.ahmadhajjar.giphy.ui.originalHeight
import me.ahmadhajjar.giphy.ui.originalWidth
import java.net.URL
import javax.swing.ImageIcon
import javax.swing.JLabel

class AppId

@Composable
@Preview
fun App(windowState: WindowState) {
    var searchTerm = mutableStateOf(TextFieldValue())
    val giphyService = mutableStateOf(GiphyService())
    val giphy = mutableStateOf(Giphy())
    val focusRequester by remember { mutableStateOf(FocusRequester()) }

    MaterialTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SearchTextField(searchTerm, giphyService, giphy, focusRequester)
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
                            giphy.value.originalHeight(360)
                        )
                        windowState.position = WindowPosition(Alignment.Center)
                        if (giphy.value.url != null) {
                            val mediaUrl = "https://i.giphy.com/media/${giphy.value.id}/giphy.gif"
                            it.icon = ImageIcon(URL(mediaUrl))
                        }
                    }
                )

            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
