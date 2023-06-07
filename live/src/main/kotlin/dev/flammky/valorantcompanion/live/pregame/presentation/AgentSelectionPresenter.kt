package dev.flammky.valorantcompanion.live.pregame.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.flammky.valorantcompanion.base.di.koin.getFromKoin
import dev.flammky.valorantcompanion.pvp.pregame.PreGameService

@Composable
fun rememberAgentSelectionPresenter(
    preGameService: PreGameService = getFromKoin()
): AgentSelectionPresenter {
    return remember(preGameService) {
        AgentSelectionPresenter(preGameService)
    }
}


class AgentSelectionPresenter(
    val preGameService: PreGameService,
) {

    @Composable
    fun present(
        livePreGameUIState: LivePreGameUIState
    ): AgentSelectionState {
        val ver = remember(livePreGameUIState.dataModContinuationKey) {
            mutableStateOf(0L)
        }.apply {
            value = livePreGameUIState.dataMod
        }
        return AgentSelectionState(
            ally = livePreGameUIState.ally,
            enemy = livePreGameUIState.enemy,
            user = livePreGameUIState.user,
            // TODO
            partyMembers = listOf(livePreGameUIState.user?.puuid ?: ""),
            selectAgent = { uuid ->
                livePreGameUIState.eventSink(LivePreGameUIState.Event.SELECT_AGENT(uuid))
            },
            lockIn = { uuid ->
                livePreGameUIState.eventSink(LivePreGameUIState.Event.LOCK_AGENT(uuid))
            },
            stateVersion = ver.value,
            stateContinuationKey = livePreGameUIState.dataModContinuationKey
        )
    }
}