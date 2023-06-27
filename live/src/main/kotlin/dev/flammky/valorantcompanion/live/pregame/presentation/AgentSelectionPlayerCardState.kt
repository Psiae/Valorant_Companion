package dev.flammky.valorantcompanion.live.pregame.presentation

import dev.flammky.valorantcompanion.assets.LocalImage

data class AgentSelectionPlayerCardState(
    val playerGameName: String,
    val playerGameNameTag: String,
    val hasSelectedAgent: Boolean,
    val selectedAgentName: String,
    val selectedAgentIcon: LocalImage<*>?,
    val selectedAgentIconKey: Any,
    val selectedAgentRoleName: String,
    val selectedAgentRoleIcon: LocalImage<*>?,
    val selectedAgentRoleIconKey: Any,
    val isLockedIn: Boolean,
    val tierName: String,
    val tierIcon: LocalImage<*>?,
    val tierIconKey: Any,
    val isUser: Boolean,
    val errorMessage: String? = null
)