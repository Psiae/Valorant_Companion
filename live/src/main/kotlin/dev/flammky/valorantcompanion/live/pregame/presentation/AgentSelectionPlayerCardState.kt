package dev.flammky.valorantcompanion.live.pregame.presentation

import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.base.C_BASE
import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead

// TODO: consider replace some with lambdas instead
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
    val competitiveTierName: String,
    val competitiveTierIcon: LocalImage<*>?,
    val competitiveTierIconKey: Any,
    val isUser: Boolean,
    val errorMessage: String? = null,
    val errorCount: Int,
    val getErrors: @SnapshotRead () -> List<AgentSelectionPlayerCardErrorMessage>
): UNSET<AgentSelectionPlayerCardState> by Companion {

    companion object : UNSET<AgentSelectionPlayerCardState> {
        override val UNSET = AgentSelectionPlayerCardState(
            playerGameName = "",
            playerGameNameTag = "",
            hasSelectedAgent = false,
            selectedAgentName = "",
            selectedAgentIcon = null,
            selectedAgentIconKey = C_BASE,
            selectedAgentRoleName = "",
            selectedAgentRoleIcon = null,
            selectedAgentRoleIconKey = C_BASE,
            isLockedIn = false,
            competitiveTierName = "",
            competitiveTierIcon = null,
            competitiveTierIconKey = C_BASE,
            isUser = false,
            errorMessage = null,
            errorCount = 0,
            getErrors = { error("no error")  }
        )
    }
}