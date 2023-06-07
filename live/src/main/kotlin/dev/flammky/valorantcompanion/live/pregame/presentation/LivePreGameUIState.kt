package dev.flammky.valorantcompanion.live.pregame.presentation

import androidx.compose.runtime.Immutable
import kotlin.time.Duration

@Immutable
data class LivePreGameUIState(
    val inPreGame: Boolean,
    val matchID: String,
    val mapName: String,
    val mapId: String,
    val gameModeName: String,
    val gamePodId: String,
    val gamePodName: String,
    val gamePodPing: Int,
    val countDown: Duration,
    val ally: PreGameTeam?,
    val enemy: PreGameTeam?,
    val user: PreGamePlayer?,
    val isProvisioned: Boolean,
    val errorMessage: String?,
    val showLoading: Boolean,
    val isAutoRefreshOn: Boolean,
    val eventSink: (Event) -> Unit,
    val dataMod: Long,
    val dataModContinuationKey: Any,
) {

    sealed interface Event {

        object USER_REFRESH : Event

        data class SET_AUTO_REFRESH(val on: Boolean): Event

        data class SELECT_AGENT(val uuid: String) : Event

        data class LOCK_AGENT(val uuid: String) : Event
    }

    companion object {
        val UNSET by lazy {
            LivePreGameUIState(
                inPreGame = false,
                matchID = "",
                errorMessage = null,
                mapName = "",
                mapId = "",
                gamePodId = "",
                gameModeName = "",
                gamePodName = "",
                gamePodPing = 0,
                countDown = Duration.INFINITE,
                ally = PreGameTeam.UNSET,
                enemy = PreGameTeam.UNSET,
                user = PreGamePlayer.UNSET,
                isProvisioned = false,
                showLoading = false,
                isAutoRefreshOn = false,
                dataMod = 0,
                dataModContinuationKey = Any(),
                eventSink = { _ ->  }
            )
        }
    }
}