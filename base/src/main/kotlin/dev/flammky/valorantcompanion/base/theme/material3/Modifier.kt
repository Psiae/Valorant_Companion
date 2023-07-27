package dev.flammky.valorantcompanion.base.theme.material3

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.localMaterial3Background() = composed {
    background(Material3Theme.backgroundColorAsState().value)
}