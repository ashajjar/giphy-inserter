package me.ahmadhajjar.giphy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import me.ahmadhajjar.giphy.config.ConfigService

@Composable
fun SettingsDialog(onClose: () -> Unit) {
    var apiKey by remember { mutableStateOf(ConfigService.apiKey) }

    Dialog(
        onCloseRequest = onClose,
        title = "Settings",
        state = rememberDialogState(width = 400.dp, height = 200.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Giphy API Settings", style = MaterialTheme.typography.h6)
            
            TextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("Giphy API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    ConfigService.apiKey = apiKey
                    onClose()
                }) {
                    Text("Save")
                }
            }
        }
    }
}
