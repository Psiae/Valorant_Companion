package dev.flammky.valorantcompanion.live.pregame.presentation

class AgentSelectionState(
    val ally: PreGameTeam?,
    val enemy: PreGameTeam?,
    val user: PreGamePlayer?,
    val partyMembers: List<String>,
    val selectAgent: (String) -> Unit,
    val lockIn: (String) -> Unit,
    val stateVersion: Long,
    val stateContinuationKey: Any
)