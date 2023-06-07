package dev.flammky.valorantcompanion.live

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.runRemember
import dev.flammky.valorantcompanion.live.match.presentation.root.LiveMatchUI
import dev.flammky.valorantcompanion.live.party.presentation.LivePartyUI
import dev.flammky.valorantcompanion.live.pregame.presentation.LivePreGame
import dev.flammky.valorantcompanion.live.pregame.presentation.rememberLivePreGamePresenter

@Composable
fun LiveMain() {

    val showPreGameScreen = remember {
        mutableStateOf(false)
    }

    LiveMainPlacement(
        showPreGame = showPreGameScreen.value,
        liveParty = {
            LivePartyUI(modifier = Modifier)
        },
        liveMatch = {
            LiveMatchUI(
                modifier = Modifier,
                openDetail = { showPreGameScreen.value = true }
            )
        },
        preGame = {
            LivePreGame(
                modifier = Modifier,
                isVisible = showPreGameScreen.value,
                state = rememberLivePreGamePresenter().present(),
                dismiss = { showPreGameScreen.value = false }
            )
        }
    )
}

@Composable
private fun LiveMainPlacement(
    showPreGame: Boolean,
    liveParty: @Composable () -> Unit,
    liveMatch: @Composable () -> Unit,
    preGame: @Composable () -> Unit
) {
    Box {
        Column(
            modifier = Modifier
                .padding(
                    top = with(LocalDensity.current) {
                        WindowInsets.statusBars.getTop(this).toDp()
                    }
                )
        ) {
            liveParty()
            liveMatch()
        }
        if (showPreGame) preGame()
    }
}