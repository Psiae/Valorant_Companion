package dev.flammky.valorantcompanion.live.ingame.presentation

import kotlinx.coroutines.Job

data class LiveInGameTeamMemberCardErrorMessage(
    val component: String,
    val message: String,
    val refresh: (() -> Job?)?
)
