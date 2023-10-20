package dev.flammky.valorantcompanion.live.pregame.presentation.preview

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.surfaceColorAsState
import dev.flammky.valorantcompanion.assets.R as AssetResource
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.live.pvp.pregame.presentation.AgentSelectionPlayerCard
import dev.flammky.valorantcompanion.live.pvp.pregame.presentation.AgentSelectionPlayerCardState

@Composable
@Preview
private fun AgentSelectionPlayerCardPreviewWithNeon() {
    val state = remember {
        AgentSelectionPlayerCardState(
            playerGameName = "Moon",
            playerGameNameTag = "0000",
            hasSelectedAgent = true,
            selectedAgentName = "Neon",
            selectedAgentIcon = LocalImage.Resource(AssetResource.raw.agent_neon_displayicon),
            selectedAgentIconKey = Unit,
            selectedAgentRoleName = "Duelist",
            selectedAgentRoleIcon = LocalImage.Resource(AssetResource.raw.agentrole_dueslist_displayicon),
            selectedAgentRoleIconKey = Unit,
            isLockedIn = false,
            competitiveTierName = "Ascendant 1",
            competitiveTierIcon = LocalImage.Resource(AssetResource.raw.rank_ascendant1_smallicon),
            competitiveTierIconKey = Unit,
            isUser = true,
            null,
            errorCount = 0,
            getErrors = { error("") }
        )
    }
    DefaultMaterial3Theme(dark = true) {
        Surface(color = Material3Theme.surfaceColorAsState().value) {
            AgentSelectionPlayerCard(state = state)
        }
    }
}