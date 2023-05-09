package dev.flammky.valorantcompanion.live.pregame.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AgentSelectionState {

    //
    // TODO: maybe skip equality check, let components decide for themselves
    //

    var ally by mutableStateOf<PreGameTeam?>(null)
        private set

    var enemy by mutableStateOf<PreGameTeam?>(null)
        private set

    fun updateAlly(team: PreGameTeam?) {
        ally = team
    }

    fun updateEnemy(team: PreGameTeam?) {
        enemy = team
    }
}