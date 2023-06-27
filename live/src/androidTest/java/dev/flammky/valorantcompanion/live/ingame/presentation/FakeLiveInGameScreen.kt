package dev.flammky.valorantcompanion.live.ingame.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme

@Composable
@Preview
private fun FakeLiveInGameScreenPreview() = DefaultMaterial3Theme(
    dynamic = true,
    dark = isSystemInDarkTheme(),
    content = { FakeLiveInGameScreen() }
)

@Composable
internal fun FakeLiveInGameScreen() = FakeLiveInGamePlacement(
    modifier = Modifier,
    background = { backgroundModifier -> FakeLiveInGameBackground(backgroundModifier) },
    content = { contentModifier -> FakeLiveInGameContent(contentModifier) }
)

@Composable
private inline fun FakeLiveInGamePlacement(
    modifier: Modifier,
    background: @Composable (Modifier) -> Unit,
    content: @Composable (Modifier) -> Unit
) = Box(modifier) {
    background(Modifier)
    content(Modifier)
}