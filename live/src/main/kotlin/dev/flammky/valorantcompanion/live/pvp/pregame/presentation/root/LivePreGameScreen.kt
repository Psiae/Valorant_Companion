package dev.flammky.valorantcompanion.live.pvp.pregame.presentation.root

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import dev.flammky.valorantcompanion.base.compose.state.subCompose
import dev.flammky.valorantcompanion.live.pvp.pregame.presentation.LivePreGameBackground
import dev.flammky.valorantcompanion.live.pvp.pregame.presentation.LivePreGameContent
import dev.flammky.valorantcompanion.live.pvp.pregame.presentation.rememberLivePreGameScreenPresenter

@Composable
fun LivePreGameScreen(
    modifier: Modifier,
    dismiss: () -> Unit,
) {
    BackHandler(true, dismiss)

    Box(
        modifier = modifier then remember {
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {}
        }
    ) {
        LivePreGameBackground(modifier = Modifier)
        subCompose {
            LivePreGameContent(
                modifier = Modifier,
                state = rememberLivePreGameScreenPresenter().present()
            )
        }
    }
}