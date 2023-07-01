package dev.flammky.valorantcompanion.live.ingame.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
internal fun LiveInGameContent(
    modifier: Modifier,
    state: LiveInGameScreenState
) = Column(modifier = modifier.fillMaxSize()) {
    LiveInGameTopBar(
        modifier = Modifier,
        mapName = state.mapName,
        gameModeName = state.gameTypeName,
        gamePodName = state.gamePodName,
        pingMs = state.gamePodPingMs?.takeIf { it > -1 },
        // TODO
        mapImage = null
    )
    if (state.matchKey != null) {
        LiveInGameTeamMembersUI(
            user = state.user,
            matchKey = state.matchKey,
            ally = state.ally,
            enemy = state.enemy
        )
    }
}