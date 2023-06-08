package dev.flammky.valorantcompanion.live.match.presentation.root

import androidx.compose.runtime.Immutable

@Immutable
data class UserMatchInfoUIState(
    val inPreGame: Boolean,
    val inGame: Boolean,
    val mapId: String,
    val mapName: String,
    val gameModeName: String,
    val gamePodName: String,
    val gamePodPingMs: Int,
    val showLoading: Boolean,
    val showLoadingOnly: Boolean,
    val errorMessage: String?,
    val needManualRefresh: Boolean,
    val autoRefreshOn: Boolean,
    val userRefresh: () -> Unit,
    val setAutoRefresh: (Boolean) -> Unit
) {

    companion object {
        val UNSET by lazy {
            UserMatchInfoUIState(
                inPreGame = false,
                inGame = false,
                mapId = "",
                mapName = "",
                gameModeName = "",
                gamePodName = "",
                gamePodPingMs = -1,
                showLoading = false,
                showLoadingOnly = false,
                errorMessage = null,
                needManualRefresh = false,
                autoRefreshOn = false,
                userRefresh = {},
                setAutoRefresh = {}
            )
        }
    }
}