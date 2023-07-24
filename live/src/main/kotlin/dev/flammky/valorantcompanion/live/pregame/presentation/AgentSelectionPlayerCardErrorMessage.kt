package dev.flammky.valorantcompanion.live.pregame.presentation

import kotlinx.coroutines.Job

data class AgentSelectionPlayerCardErrorMessage(
    val component: String,
    val message: String,
    val refresh: (() -> Job?)?
)
