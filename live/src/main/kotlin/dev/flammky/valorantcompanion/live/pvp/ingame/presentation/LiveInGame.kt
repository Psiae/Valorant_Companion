package dev.flammky.valorantcompanion.live.pvp.ingame.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
internal fun LiveInGame(
    modifier: Modifier,
    dismiss: () -> Unit
) {
    // TODO
    BackHandler(onBack = dismiss)

    val state = rememberLiveInGameScreenPresenter().present()

    LiveInGamePlacement(
        modifier = modifier,
        background = { bgModifier -> LiveInGameBackground(bgModifier) },
        content = { contentModifier -> LiveInGameContent(contentModifier, state) }
    )
}

@Composable
private fun LiveInGamePlacement(
    modifier: Modifier,
    background: @Composable (Modifier) -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Box(modifier.fillMaxSize().pointerInput(Unit) {}) {
        background(Modifier)
        content(Modifier)
    }
}