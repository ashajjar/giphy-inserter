package me.ahmadhajjar.giphy.ui

import androidx.compose.ui.unit.dp
import me.ahmadhajjar.giphy.service.Giphy

fun Giphy.originalWidth(default: Int) = ((this.images?.original?.width?.toInt() ?: default)).dp
fun Giphy.originalHeight(default: Int) = ((this.images?.original?.height?.toInt() ?: default) + 55).dp
