package dev.flammky.valorantcompanion.live.pvp.pregame.presentation

import kotlinx.coroutines.Job

data class AgentSelectionPlayerCardErrorMessage(
    val component: String,
    val message: String,
    val refresh: (() -> Job?)?
)
