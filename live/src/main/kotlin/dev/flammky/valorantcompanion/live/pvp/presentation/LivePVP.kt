package dev.flammky.valorantcompanion.live.pvp.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.live.main.LiveMainScreenContainer
import dev.flammky.valorantcompanion.live.pvp.match.presentation.root.LiveMatchUI
import dev.flammky.valorantcompanion.live.pvp.party.presentation.LivePartyUI
import dev.flammky.valorantcompanion.live.pvp.ingame.presentation.LiveInGame
import dev.flammky.valorantcompanion.live.pvp.pregame.presentation.root.LivePreGameScreen

@Composable
fun LivePVP(
    modifier: Modifier,
    openScreen: (@Composable LiveMainScreenContainer.() -> Unit) -> Unit
) {

    val openPreGameScreen = remember(openScreen) {
        { openScreen.invoke(livePreGameScreen()) }
    }

    val openInGameDetail = remember(openScreen) {
        { openScreen.invoke(liveInGameScreen()) }
    }

    LiveMainPlacement(
        modifier = modifier,
        liveParty = {
            LivePartyUI(modifier = Modifier)
        },
        liveMatch = {
            LiveMatchUI(
                modifier = Modifier,
                openPreGameDetail = openPreGameScreen,
                openInGameDetail = openInGameDetail
            )
        },
    )
}

private fun livePreGameScreen(): @Composable LiveMainScreenContainer.() -> Unit {
    val fn: @Composable LiveMainScreenContainer.() -> Unit = {
        LivePreGameScreen(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding(),
            dismiss = ::dismiss
        )
    }
    return fn
}

private fun liveInGameScreen(): @Composable LiveMainScreenContainer.() -> Unit {
    val fn: @Composable LiveMainScreenContainer.() -> Unit = {
        LiveInGame(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding(),
            dismiss = ::dismiss
        )
    }
    return fn
}

@Composable
private fun LiveMainPlacement(
    modifier: Modifier,
    liveParty: @Composable () -> Unit,
    liveMatch: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.statusBarsPadding()
    ) {
        liveParty()
        liveMatch()
    }
}