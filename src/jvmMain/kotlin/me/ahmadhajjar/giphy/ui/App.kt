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
import androidx.compose.ui.unit.dp
import me.ahmadhajjar.giphy.service.Giphy
import me.ahmadhajjar.giphy.service.GiphyService
import me.ahmadhajjar.giphy.ui.SearchTextField
import java.net.URL
import javax.swing.ImageIcon
import javax.swing.JLabel

@Composable
@Preview
fun App() {
    var searchTerm = mutableStateOf(TextFieldValue())
    val giphyService = mutableStateOf(GiphyService())
    val giphy = mutableStateOf<Giphy?>(Giphy())
    val focusRequester by remember { mutableStateOf(FocusRequester()) }

    MaterialTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SearchTextField(searchTerm, giphyService, giphy, focusRequester)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                SwingPanel(
                    modifier = Modifier.size(
                        (giphy.value?.images?.original?.width?.toInt() ?: 500).dp,
                        (giphy.value?.images?.original?.height?.toInt() ?: 500).dp
                    ),
                    factory = {
                        giphy.value = giphyService.value.nextGiphy("Hello World!")
                        println(giphy.value?.url)
                        if (giphy.value?.url != null) {
                            JLabel(ImageIcon(URL(giphy.value?.url)))
                        } else {
                            JLabel(ImageIcon(URL("https://i.giphy.com/media/E1w0yvMxBIv5M8WkL8/giphy.gif")))
                        }
                    },
                    update = {
                        println(giphy.value?.url)
                        if (giphy.value?.url != null) {
                            val mediaUrl = "https://i.giphy.com/media/${giphy.value?.id}/giphy.gif"
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
