package dev.flammky.valorantcompanion.live.match.presentation.root

import androidx.compose.runtime.Immutable

@Immutable
data class UserMatchInfoUIState(
    val inPreGame: Boolean,
    val inGame: Boolean,
    val mapName: String,
    val gameModeName: String,
    val gamePodName: String,
    val gamePodPingMs: Int,
    val showLoading: Boolean,
    val errorMessage: String?
) {

    companion object {
        val UNSET by lazy {
            UserMatchInfoUIState(
                inPreGame = false,
                inGame = false,
                mapName = "",
                gameModeName = "",
                gamePodName = "",
                gamePodPingMs = -1,
                showLoading = false,
                errorMessage = null
            )
        }
    }
}