package dev.flammky.valorantcompanion.live.store.presentation.agent.cpreview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.live.store.presentation.agent.AgentDisplayCard

@Composable
@Preview
fun AgentDisplayCardPreview(
    
) {
    DefaultMaterial3Theme(
        dark = true
    ) {
        AgentDisplayCard(
            modifier = Modifier,
            contentPadding = PaddingValues(16.dp),
            agentDisplayImageModifier = Modifier
                .size((188.5 - 16).dp),
            agentDisplayImageKey = Any(),
            agentDisplayImage = LocalImage.Resource(R_ASSET_RAW.agent_neon_displayicon),
            agentRoleDisplayImageKey = Any(),
            agentRoleDisplayImage = LocalImage.Resource(R_ASSET_RAW.agentrole_dueslist_displayicon),
            canOpenDetail = true,
            openDetail = {},
            agentName = "NEON",
            owned = false,
        )
    }
}