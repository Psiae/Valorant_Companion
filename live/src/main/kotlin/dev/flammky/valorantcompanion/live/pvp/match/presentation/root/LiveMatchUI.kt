package dev.flammky.valorantcompanion.live.pvp.match.presentation.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LiveMatchUI(
    modifier: Modifier,
    openPreGameDetail: () -> Unit,
    openInGameDetail: () -> Unit,
    visibleToUser: Boolean
) {
    UserMatchInfoUI(
        modifier = modifier,
        state = rememberUserMatchInfoPresenter().present(visibleToUser),
        openPreGameDetail = openPreGameDetail,
        openInGameDetail = openInGameDetail
    )
}
