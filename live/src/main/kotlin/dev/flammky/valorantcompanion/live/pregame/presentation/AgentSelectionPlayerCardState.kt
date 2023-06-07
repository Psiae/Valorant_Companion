package dev.flammky.valorantcompanion.live.pregame.presentation

import dev.flammky.valorantcompanion.live.shared.presentation.LocalImageData
import java.io.File

data class AgentSelectionPlayerCardState(
    val playerGameName: String,
    val playerGameNameTag: String,
    val hasSelectedAgent: Boolean,
    val selectedAgentName: String,
    val selectedAgentIcon: LocalImageData<*>?,
    val selectedAgentIconKey: Any,
    val selectedAgentRoleName: String,
    val selectedAgentRoleIcon: LocalImageData<*>?,
    val selectedAgentRoleIconKey: Any,
    val isLockedIn: Boolean,
    val tierName: String,
    val tierIcon: LocalImageData<*>?,
    val tierIconKey: Any,
    val isUser: Boolean,
    val errorMessage: String? = null
)