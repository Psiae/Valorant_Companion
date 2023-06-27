package dev.flammky.valorantcompanion.live.match.presentation

import androidx.compose.runtime.Composable

@Composable
fun CurrentMatchScreen(
    state: CurrentMatchState
) {
    CurrentMatchScreenPlacement(
        topBar = { CurrentMatchTopBar() },
    )
}

@Composable
private fun CurrentMatchScreenPlacement(
    topBar: @Composable () -> Unit
) {

}