package dev.flammky.valorantcompanion.live.pregame.presentation.preview

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.base.rememberThis
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.surfaceColorAsState
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgent
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import dev.flammky.valorantcompanion.assets.R as R_ASSET

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MockableAgentSelectionPool(
    modifier: Modifier,
    // list of agent Id's that the player has unlocked
    unlockedAgents: List<String>,
    // list of agent Id's that is locked by teammates or other reason
    disabledAgents: List<String>,
    // list of available agent within the pool
    agentPool: List<ValorantAgent>,
    selectedAgent: String?,
    onSelected: (String) -> Unit
) {
    val upOnSelected = rememberUpdatedState(newValue = onSelected)
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val unlockedInPool = mutableListOf<@Composable () -> Unit>()
        val notUnlockedInPool = mutableListOf<@Composable () -> Unit>()
        agentPool.forEach { agent ->
            val identity = ValorantAgentIdentity.of(agent)
            val userAgent = identity.uuid == selectedAgent
            val unlocked = unlockedAgents.any { it == identity.uuid }
            val disabled = disabledAgents.any { it == identity.uuid }
            val block = @Composable {
                SelectableAgentIcon(
                    modifier = Modifier.size(45.dp),
                    enabled = !userAgent && unlocked && !disabled,
                    locked = !unlocked,
                    data = LocalImage.Resource(agentIconOf(agent)),
                    dataKey = agent,
                    agentName = identity.displayName,
                    onSelected = { upOnSelected.value(identity.uuid) }
                )
            }
            if (unlocked) unlockedInPool.add(block) else notUnlockedInPool.add(block)
        }
        unlockedInPool.forEach { composable -> composable.invoke() }
        notUnlockedInPool.forEach { composable -> composable.invoke() }
    }
}

@Composable
@Preview
private fun MockableAgentSelectionPoolPreview() {
    val selectedState = remember {
        mutableStateOf(ValorantAgentIdentity.of(ValorantAgent.NEON).uuid)
    }
    DefaultMaterial3Theme(
        dark = true
    ) {
        Surface(
            color = Material3Theme.surfaceColorAsState().value
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                MockableAgentSelectionPool(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(10.dp),
                    unlockedAgents = remember {
                        ValorantAgent.asList().filter { agent ->
                            // Agents I use the least
                            agent != ValorantAgent.HARBOR &&
                                    agent != ValorantAgent.ASTRA &&
                                    agent != ValorantAgent.CYPHER &&
                                    agent != ValorantAgent.BREACH
                        }.map { ValorantAgentIdentity.of(it).uuid }
                    },
                    disabledAgents = remember {
                        listOf(
                            ValorantAgent.JETT,
                            ValorantAgent.CHAMBER,
                        ).map { ValorantAgentIdentity.of(it).uuid }
                    },
                    agentPool = ValorantAgent.asList(),
                    selectedAgent = selectedState.value,
                    onSelected = { selectedState.value = it }
                )
            }
        }
    }
}


private fun agentIconOf(agent: ValorantAgent): Int {
    return when(agent) {
        ValorantAgent.ASTRA -> R_ASSET.raw.agent_astra_displayicon
        ValorantAgent.BREACH -> R_ASSET.raw.agent_breach_displayicon
        ValorantAgent.BRIMSTONE -> R_ASSET.raw.agent_brimstone_displayicon
        ValorantAgent.CHAMBER -> R_ASSET.raw.agent_chamber_displayicon
        ValorantAgent.CYPHER -> R_ASSET.raw.agent_cypher_displayicon
        ValorantAgent.FADE -> R_ASSET.raw.agent_fade_displayicon
        ValorantAgent.GEKKO -> R_ASSET.raw.agent_gekko_displayicon
        ValorantAgent.HARBOR -> R_ASSET.raw.agent_harbor_displayicon
        ValorantAgent.JETT -> R_ASSET.raw.agent_jett_displayicon
        ValorantAgent.KAYO -> R_ASSET.raw.agent_kayo_displayicon
        ValorantAgent.KILLJOY -> R_ASSET.raw.agent_killjoy_displayicon
        ValorantAgent.NEON -> R_ASSET.raw.agent_neon_displayicon
        ValorantAgent.OMEN -> R_ASSET.raw.agent_omen_displayicon
        ValorantAgent.PHOENIX -> R_ASSET.raw.agent_phoenix_displayicon
        ValorantAgent.RAZE -> R_ASSET.raw.agent_raze_displayicon
        ValorantAgent.REYNA -> R_ASSET.raw.agent_reyna_displayicon
        ValorantAgent.SAGE -> R_ASSET.raw.agent_sage_displayicon
        ValorantAgent.SKYE -> R_ASSET.raw.agent_skye_displayicon
        ValorantAgent.SOVA -> R_ASSET.raw.agent_sova_displayicon
        ValorantAgent.VIPER -> R_ASSET.raw.agent_viper_displayicon
        ValorantAgent.YORU -> R_ASSET.raw.agent_yoru_displayicon
    }
}

@Composable
private fun SelectableAgentIcon(
    modifier: Modifier,
    enabled: Boolean,
    locked: Boolean,
    data: LocalImage<*>?,
    dataKey: Any,
    agentName: String,
    onSelected: () -> Unit
) {
    val hash = remember {
        mutableStateOf(0)
    }.rememberThis(dataKey) {
        value++
    }
    val ctx = LocalContext.current
    val inspection = LocalInspectionMode.current
    Box(
        modifier = Modifier.border(
            1.dp,
            Color.White.copy(alpha = 0.5f),
        )
    ) {
        Box(
            modifier = modifier
                .rememberThis(enabled) {
                    if (enabled) {
                        clickable(
                            enabled = true,
                            onClick = onSelected,
                        )
                    } else {
                        alpha(0.38f)
                    }
                }
        ) {
            AsyncImage(
                model = remember(hash, inspection) {
                    ImageRequest.Builder(ctx)
                        .setParameter("retry_hash", hash)
                        .run {
                            if (inspection && data is LocalImage.Resource) {
                                placeholder(data.value)
                            } else {
                                data(data?.value)
                            }
                        }
                        .build()
                },
                contentDescription = "Agent Icon of $agentName"
            )
        }
        if (locked) {
            Icon(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp),
                painter = painterResource(id = R_ASSET.drawable.lock_fill0_wght400_grad0_opsz48),
                contentDescription = "Locked Agent Icon of $agentName",
                tint = Color.White
            )
        }
    }
}