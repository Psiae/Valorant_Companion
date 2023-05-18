package dev.flammky.valorantcompanion.live.pregame.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AgentSelectionState(
    val ally: PreGameTeam,
    val enemy: PreGameTeam,
    val user: PreGamePlayer,
    val selectAgent: (String) -> Unit,
    val lockIn: () -> Unit
)