package dev.flammky.valorantcompanion.live.pregame.presentation

import androidx.compose.runtime.Immutable
import kotlin.time.Duration

@Immutable
data class LivePreGameUIState(
    val inPreGame: Boolean,
    val mapName: String,
    val gameModeName: String,
    val gamePodName: String,
    val gamePodPing: Int,
    val countDown: Duration,
    val ally: PreGameTeam?,
    val enemy: PreGameTeam?,
    val isProvisioned: Boolean,
    val errorMessage: String?,
    val showLoading: Boolean,
    val isAutoRefreshOn: Boolean,
    val eventSink: (Event) -> Unit
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
                errorMessage = null,
                mapName = "",
                gameModeName = "",
                gamePodName = "",
                gamePodPing = 0,
                countDown = Duration.INFINITE,
                ally = PreGameTeam.UNSET,
                enemy = PreGameTeam.UNSET,
                isProvisioned = false,
                showLoading = false,
                isAutoRefreshOn = false,
                eventSink = { _ ->  }
            )
        }
    }
}