package dev.flammky.valorantcompanion.live.pregame.presentation.preview

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.live.pingStrengthInRangeOf4
import dev.flammky.valorantcompanion.live.pregame.presentation.*
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.live.pvp.pregame.presentation.*
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgent
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentRole
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import dev.flammky.valorantcompanion.assets.R as R_ASSET


@Composable
fun MockableTeamAgentSelectionColumn() {

    DefaultMaterial3Theme {
        Surface(
            color = Material3Theme.surfaceColorAsState().value
        ) {

        }
    }
}

@Composable
@Preview(
    name = "Team Agent Selection",
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF1A1C1E
)
fun TeamAgentSelectionColumnPreview(
     user: String = "Dokka",
     partyMembers: ImmutableList<String> = persistentListOf("Dokka", "Dex")
) {
    // TODO: state factory
    val mockedState = rememberMockedAgentSelectionPresenter().present()
    DefaultMaterial3Theme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Material3Theme.surfaceColorAsState().value
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                TopBarPreview(modifier = Modifier)
                Spacer(modifier = Modifier.height(10.dp))
                Column {
                    mockedState.ally?.players?.forEachIndexed { i, player ->
                        key(i, player) {
                            val name = playerNameFromMockedPUUID(player.puuid)
                            val tag = "0000"
                            val charID = player.characterID
                            AgentSelectionPlayerCard(
                                state = mockPlayerCardState(
                                    index = i,
                                    name = name,
                                    nameTag = tag,
                                    maskName = player.identity.incognito && player.puuid !in partyMembers,
                                    tier = player.competitiveTier,
                                    lockedIn =  player.characterSelectionState == CharacterSelectionState.LOCKED,
                                    selectedAgentName = if (player.characterSelectionState != CharacterSelectionState.NONE) {
                                        agentNameFromMockedAgentId(charID)
                                    } else {
                                        ""
                                    },
                                    selectedAgentRoleName = agentRoleFromMockedAgentId(charID)?.displayName ?: "",
                                    isUser = player == mockedState.user
                                )
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LockInButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    agentName = agentNameFromMockedAgentId(mockedState.user?.characterID ?: ""),
                    enabled = mockedState.user != null && mockedState.user?.characterSelectionState != CharacterSelectionState.LOCKED,
                    onClick = { mockedState.lockIn(mockedState.user?.characterID ?: "") }
                )
                Spacer(modifier = Modifier.height(20.dp))
                MockableAgentSelectionPool(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    unlockedAgents = remember {
                        ValorantAgent.asList().filter { agent ->
                            // Agents I use the least
                            agent != ValorantAgent.HARBOR &&
                                    agent != ValorantAgent.ASTRA &&
                                    agent != ValorantAgent.CYPHER &&
                                    agent != ValorantAgent.BREACH
                        }.map { ValorantAgentIdentity.of(it).uuid }
                    },
                    disabledAgents = mockedState.ally?.players?.map { it.characterID } ?: emptyList(),
                    agentPool = ValorantAgent.asList(),
                    selectedAgent = mockedState.user?.characterID,
                    onSelected = mockedState.selectAgent
                )
            }
        }
    }
}

@Composable
@Preview(
    uiMode = UI_MODE_NIGHT_YES,
)
private fun TopBarPreview(
    modifier: Modifier = Modifier
) {
    val stepTimeLeft = 100f
    DefaultMaterial3Theme {
        Surface(
            modifier = modifier,
            color = Material3Theme.surfaceColorAsState().value
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    modifier = Modifier.matchParentSize(),
                    model = R_ASSET.raw.ascent_splash,
                    contentDescription = "Map Ascent",
                    contentScale = ContentScale.Crop,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f, false)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Material3Theme.surfaceVariantColorAsState().value.copy(alpha = 0.97f))
                            .padding(12.dp)
                    ) {
                        val textColor = if (LocalIsThemeDark.current) Color.White else Color.Black
                        Text(
                            "Map - AscentTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT".uppercase(),
                            color = textColor,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "Spike Rush".uppercase(),
                            color = textColor,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row {
                            Text(
                                modifier = Modifier.weight(1f, false),
                                text = "Singapore-1",
                                color = textColor,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                modifier = Modifier.weight(1f, false),
                                text = "(25ms)",
                                color = textColor,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(1.dp))
                            Draw4PingBar(modifier = Modifier
                                .height(8.dp)
                                .align(Alignment.CenterVertically), pingMs = 25)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .align(Alignment.CenterVertically)
                            .clip(CircleShape)
                            .background(Material3Theme.surfaceVariantColorAsState().value.copy(alpha = 0.97f))
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center),
                            progress = stepTimeLeft / 85,
                            strokeWidth = 2.dp,
                            color = if (stepTimeLeft <= 10f) Color.Red else Color.Green
                        )
                        val text = stepTimeLeft.toInt().toString()
                        Text(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(6.dp),
                            text = text,
                            textAlign = TextAlign.Center,
                            color = if (LocalIsThemeDark.current) Color.White else Color.Black,
                            style = MaterialTheme.typography.labelLarge
                                .copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize =  when (text.length) {
                                        1 -> 43.sp
                                        else -> ((68.dp - 6.dp - 2.dp) / text.length).value.sp
                                    }.also {
                                        Log.d(
                                            "LivePreGame.kt",
                                            "TopBarInfo_countDown@: fontSize=$it")
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LockInButton(
    modifier: Modifier,
    agentName: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFF4444C).copy(alpha = if (enabled) 1f else 0.38f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "Lock $agentName".uppercase(),
            style = MaterialTheme
                .typography
                .labelLarge
                .copy(
                    color = Color.White.copy(alpha = if (enabled) 1f else 0.38f)
                )
        )
    }
}

@Composable
private fun AgentSelectionLocks(
    allies: List<Boolean>,
    enemies: List<Boolean>
) {

}

private fun mockPlayerCardState(
    index: Int,
    name: String,
    nameTag: String,
    maskName: Boolean,
    tier: Int,
    lockedIn: Boolean,
    selectedAgentName: String,
    selectedAgentRoleName: String,
    isUser: Boolean
): AgentSelectionPlayerCardState = AgentSelectionPlayerCardState(
    playerGameName = name.takeIf { !maskName } ?: "Player ${index + 1}",
    playerGameNameTag = nameTag.takeIf { !maskName } ?: "",
    hasSelectedAgent = true,
    selectedAgentName = selectedAgentName,
    selectedAgentIcon = LocalImage.Resource(agentDisplayIcon(selectedAgentName)),
    selectedAgentIconKey = Unit,
    selectedAgentRoleName = selectedAgentRoleName,
    selectedAgentRoleIcon = LocalImage.Resource(agentRoleIconForName(selectedAgentRoleName)),
    selectedAgentRoleIconKey = Unit,
    isLockedIn = lockedIn,
    competitiveTierName = "",
    competitiveTierIcon = LocalImage.Resource(latestPatchTierIcon(tier)),
    competitiveTierIconKey = Unit,
    isUser = isUser,
    errorCount = 0,
    getErrors = { error("") }
)

private fun playerNameFromMockedPUUID(
    puuid: String
) = puuid

private fun agentNameFromMockedAgentId(
    agentID: String
): String {
    var name = ""
    run {
        ValorantAgentIdentity.iter().forEach { identity ->
            if (identity.uuid == agentID) {
                name = identity.displayName
                return@run
            }
        }
    }
    return name
}

private fun agentRoleFromMockedAgentId(
    agentID: String
): ValorantAgentRole?  {
    val identities = ValorantAgentIdentity.asList()
    return identities.find { it.uuid == agentID }?.role
}

private fun mockPreGamePlayer(
    puuid: String,
    characterId: String,
    selectionState: CharacterSelectionState,
    preGamePlayerState: PreGamePlayerState,
    competitiveTier: Int,
    incognito: Boolean,
    isCaptain: Boolean
): PreGamePlayer = PreGamePlayer(
    puuid = puuid,
    characterID = characterId,
    characterSelectionState = selectionState,
    pregamePlayerState = preGamePlayerState,
    competitiveTier = competitiveTier,
    identity = PreGamePlayerInfo(puuid, "", "", 100, "", incognito, false),
    seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
    isCaptain = isCaptain
)

private fun latestPatchTierIcon(tier: Int): Int {
    return when(tier) {
        0, 1, 2 -> R_ASSET.raw.rank_unranked_smallicon
        3 -> R_ASSET.raw.rank_iron1_smallicon
        4 -> R_ASSET.raw.rank_iron2_smallicon
        5 -> R_ASSET.raw.rank_iron3_smallicon
        6 -> R_ASSET.raw.rank_bronze1_smallicon
        7 -> R_ASSET.raw.rank_bronze2_smallicon
        8 -> R_ASSET.raw.rank_bronze3_smallicon
        9 -> R_ASSET.raw.rank_silver1_smallicon
        10 -> R_ASSET.raw.rank_silver2_smallicon
        11 -> R_ASSET.raw.rank_silver3_smallicon
        12-> R_ASSET.raw.rank_gold1_smallicon
        13 -> R_ASSET.raw.rank_gold2_smallicon
        14 -> R_ASSET.raw.rank_gold3_smallicon
        15 -> R_ASSET.raw.rank_platinum1_smallicon
        16 -> R_ASSET.raw.rank_platinum2_smallicon
        17 -> R_ASSET.raw.rank_platinum3_smallicon
        18 -> R_ASSET.raw.rank_diamond1_smallicon
        19 -> R_ASSET.raw.rank_diamond2_smallicon
        20 -> R_ASSET.raw.rank_diamond3_smallicon
        21 -> R_ASSET.raw.rank_ascendant1_smallicon
        22 -> R_ASSET.raw.rank_ascendant2_smallicon
        23 -> R_ASSET.raw.rank_ascendant3_smallicon
        24 -> R_ASSET.raw.rank_immortal1_smallicon
        25 -> R_ASSET.raw.rank_immortal2_smallicon
        26 -> R_ASSET.raw.rank_immortal3_smallicon
        27 -> R_ASSET.raw.rank_radiant_smallicon
        else -> 0
    }
}

private fun agentDisplayIcon(agentName: String): Int {
    // TODO: codename as well
    return when(agentName.lowercase()) {
        "astra" -> R_ASSET.raw.agent_astra_displayicon
        "breach" -> R_ASSET.raw.agent_breach_displayicon
        "brimstone" -> R_ASSET.raw.agent_brimstone_displayicon
        "chamber" -> R_ASSET.raw.agent_chamber_displayicon
        "cypher" -> R_ASSET.raw.agent_cypher_displayicon
        "fade" -> R_ASSET.raw.agent_fade_displayicon
        "gekko" -> R_ASSET.raw.agent_gekko_displayicon
        "harbor" -> R_ASSET.raw.agent_harbor_displayicon
        "jett" -> R_ASSET.raw.agent_jett_displayicon
        "kayo", "kay/o", "grenadier" -> R_ASSET.raw.agent_grenadier_displayicon
        "killjoy" -> R_ASSET.raw.agent_killjoy_displayicon
        "neon" -> R_ASSET.raw.agent_neon_displayicon
        "omen" -> R_ASSET.raw.agent_omen_displayicon
        "phoenix" -> R_ASSET.raw.agent_phoenix_displayicon
        "raze" -> R_ASSET.raw.agent_raze_displayicon
        "reyna" -> R_ASSET.raw.agent_reyna_displayicon
        "sage" -> R_ASSET.raw.agent_sage_displayicon
        "skye" -> R_ASSET.raw.agent_skye_displayicon
        "sova" -> R_ASSET.raw.agent_sova_displayicon
        "viper" -> R_ASSET.raw.agent_viper_displayicon
        "yoru" -> R_ASSET.raw.agent_yoru_displayicon
        else -> 0
    }
}

private fun agentRoleIconForName(name: String): Int {
    return when(name.lowercase()) {
        "controller" -> R_ASSET.raw.role_controller_displayicon
        "duelist" -> R_ASSET.raw.role_duelist_displayicon
        "initiator" -> R_ASSET.raw.role_initiator_displayicon
        "sentinel" -> R_ASSET.raw.role_sentinel_displayicon
        else -> 0
    }
}

@Composable
private fun Draw4PingBar(
    modifier: Modifier,
    pingMs: Int
) {
    val strength = pingStrengthInRangeOf4(pingMs)
    check(strength in 1..4)
    val color = when (strength) {
        1 -> Color.Red
        2 -> Color.Yellow
        3, 4 -> Color.Green
        else -> error("Unguarded condition")
    }
    BoxWithConstraints(
        modifier = modifier.size(24.dp)
    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(4) { i ->
                val n = i + 1
                val height = (maxHeight.value * (n.toFloat() / 4)).dp
                val width = maxHeight / 4
                Box(
                    modifier = Modifier
                        .height(height)
                        .width(width)
                        .background(color.takeIf { strength >= n } ?: Color.Gray)
                        .shadow(2.dp)
                )
            }
        }
    }
}