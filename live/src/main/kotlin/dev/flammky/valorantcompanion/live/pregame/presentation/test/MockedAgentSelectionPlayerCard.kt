package dev.flammky.valorantcompanion.live.pregame.presentation.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
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
            selectedAgentIcon = LocalImageData.Resource(AssetResource.raw.neon_displayicon),
            selectedAgentIconKey = Unit,
            isLockedIn = false,
            tierName = "Ascendant 1",
            tierIcon = LocalImageData.Resource(AssetResource.raw.ascendant1_smallicon),
            tierIconKey = Unit,
            isUser = true
        )
    }
    DefaultMaterial3Theme(dark = true) {
        AgentSelectionPlayerCard(state = state)
    }
}