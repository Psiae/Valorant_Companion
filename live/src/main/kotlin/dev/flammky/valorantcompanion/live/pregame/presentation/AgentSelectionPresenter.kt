package dev.flammky.valorantcompanion.live.pregame.presentation

import androidx.compose.runtime.Composable
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
        ally: PreGameTeam?,
        enemy: PreGameTeam?
    ): AgentSelectionState {
        val state = remember(this) { AgentSelectionState() }

        state
            .apply {
                updateAlly(ally)
                updateEnemy(enemy)
            }

        return state
    }
}