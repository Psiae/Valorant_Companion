package dev.flammky.valorantcompanion.live.pregame.presentation.preview

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.surfaceColorAsState
import dev.flammky.valorantcompanion.assets.R as AssetResource
import dev.flammky.valorantcompanion.live.pregame.presentation.AgentSelectionPlayerCard
import dev.flammky.valorantcompanion.live.pregame.presentation.AgentSelectionPlayerCardState
import dev.flammky.valorantcompanion.live.shared.presentation.LocalImageData

@Composable
@Preview
private fun AgentSelectionPlayerCardPreviewWithNeon() {
    val state = remember {
        AgentSelectionPlayerCardState(
            playerGameName = "Moon",
            playerGameNameTag = "0000",
            hasSelectedAgent = true,
            selectedAgentName = "Neon",
            selectedAgentIcon = LocalImageData.Resource(AssetResource.raw.agent_neon_displayicon),
            selectedAgentIconKey = Unit,
            selectedAgentRoleName = "Duelist",
            selectedAgentRoleIcon = LocalImageData.Resource(AssetResource.raw.role_duelist_displayicon),
            selectedAgentRoleIconKey = Unit,
            isLockedIn = false,
            tierName = "Ascendant 1",
            tierIcon = LocalImageData.Resource(AssetResource.raw.rank_ascendant1_smallicon),
            tierIconKey = Unit,
            isUser = true
        )
    }
    DefaultMaterial3Theme(dark = true) {
        Surface(color = Material3Theme.surfaceColorAsState().value) {
            AgentSelectionPlayerCard(state = state)
        }
    }
}