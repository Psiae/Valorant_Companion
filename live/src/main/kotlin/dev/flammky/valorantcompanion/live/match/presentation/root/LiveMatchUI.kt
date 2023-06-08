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
    UserMatchInfoUI(
        modifier = modifier,
        state = rememberUserMatchInfoPresenter().present(),
        openDetail = openDetail,
    )
}
