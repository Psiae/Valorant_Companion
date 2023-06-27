package dev.flammky.valorantcompanion.live.match.presentation.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LiveMatchUI(
    modifier: Modifier,
    openPreGameDetail: () -> Unit,
    openInGameDetail: () -> Unit
) {
    UserMatchInfoUI(
        modifier = modifier,
        state = rememberUserMatchInfoPresenter().present(),
        openPreGameDetail = openPreGameDetail,
        openInGameDetail = openInGameDetail
    )
}
