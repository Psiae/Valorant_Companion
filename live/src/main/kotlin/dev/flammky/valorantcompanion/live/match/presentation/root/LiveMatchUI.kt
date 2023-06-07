package dev.flammky.valorantcompanion.live.match.presentation.root

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.flammky.valorantcompanion.live.pregame.presentation.LivePreGameUIState
import dev.flammky.valorantcompanion.live.pregame.presentation.rememberLivePreGamePresenter

@Composable
fun LiveMatchUI(
    modifier: Modifier,
    openDetail: () -> Unit
) {
    val preGamePresenter = rememberLivePreGamePresenter()
    // TODO: val inGamePresenter = rememberLiveInGamePresenter()
    val preGameUIState = preGamePresenter.present()
    UserMatchInfoUI(
        modifier = modifier,
        state = presentUserMatchInfoUI(preGameUIState = preGameUIState),
        openDetail = openDetail,
        refresh = {
            preGameUIState.eventSink(LivePreGameUIState.Event.USER_REFRESH)
        }
    )
}

@Composable
private fun presentUserMatchInfoUI(
    preGameUIState: LivePreGameUIState
): UserMatchInfoUIState  {
    Log.d("LiveMatchUI.kt", "presentUserMatchInfoUI($preGameUIState)")
    val state = remember {
        mutableStateOf(UserMatchInfoUIState.UNSET)
    }
    return remember(preGameUIState) {
        state.apply {
            value = UserMatchInfoUIState(
                inPreGame = preGameUIState.inPreGame,
                inGame = false,
                mapName = preGameUIState.mapName,
                gameModeName = preGameUIState.gameModeName,
                gamePodName = preGameUIState.gamePodName,
                gamePodPingMs = preGameUIState.gamePodPing,
                showLoading = preGameUIState.showLoading,
                errorMessage = preGameUIState.errorMessage
            )
        }
    }.value
}