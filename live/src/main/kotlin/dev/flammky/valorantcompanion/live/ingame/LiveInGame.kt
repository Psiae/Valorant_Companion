package dev.flammky.valorantcompanion.live.ingame

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun LiveInGame(
    modifier: Modifier,
    dismiss: () -> Unit
) {
    // TODO
    BackHandler(onBack = dismiss)

    LiveInGamePlacement(
        modifier = modifier,
        background = { LiveInGameBackground(it) },
        content = { LiveInGameContent(it) }
    )
}

@Composable
private fun LiveInGamePlacement(
    modifier: Modifier,
    background: @Composable (Modifier) -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Box(modifier) {
        background(Modifier.fillMaxSize())
        content(Modifier.fillMaxSize())
    }
}