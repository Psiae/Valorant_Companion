package dev.flammky.valorantcompanion.live.pregame.presentation.preview

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.surfaceColorAsState
import dev.flammky.valorantcompanion.live.pingStrengthInRangeOf4
import dev.flammky.valorantcompanion.assets.R as R_ASSET
import dev.flammky.valorantcompanion.live.pregame.presentation.*
import dev.flammky.valorantcompanion.live.shared.presentation.LocalImageData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
@Preview(
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF1A1C1E
)
private fun TeamAgentSelectionColumnPreview(
    // user: userPUUID,
    // partyMembers: ImmutableList<String>
) {
    DefaultMaterial3Theme {
        Surface(
            color = Material3Theme.surfaceColorAsState().value
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                TopBarPreview(
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                mockedAgentSelectionColumnState().ally?.players?.forEachIndexed { i, player ->
                    key(player.puuid) {
                        val name = playerNameFromMockedPUUID(player.puuid)
                        val tag = "0000"
                        AgentSelectionPlayerCard(
                            state = mockPlayerCardState(
                                index = i,
                                name = name,
                                nameTag = tag,
                                incognito = player.identity.incognito,
                                tier = player.competitiveTier,
                                lockedIn =  player.characterSelectionState == CharacterSelectionState.LOCKED,
                                selectedAgentName = if (player.characterSelectionState != CharacterSelectionState.NONE) {
                                    agentNameFromMockedAgentId(player.characterID)
                                } else {
                                    ""
                                },
                                selectedAgentRoleName = agentRoleFromMockedAgentId(player.characterID),
                                isUser = name == "Dokka" && tag == "0000"
                            )
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
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
    DefaultMaterial3Theme {
        Surface(
            modifier = modifier,
            color = Material3Theme.surfaceColorAsState().value
        ) {
            Column {
                val textColor = if (LocalIsThemeDark.current) Color.White else Color.Black
                Text(
                    "Map - Ascent".uppercase(),
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
                        "Singapore-1",
                        color = textColor,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        "(25ms)",
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
        }
    }
}

@Composable
private fun AgentSelectionLocks(
    allies: List<Boolean>,
    enemies: List<Boolean>
) {

}

@Composable
private fun mockedAgentSelectionColumnState(): AgentSelectionState {
    val state = remember {
        AgentSelectionState()
            .apply {
                val ally = PreGameTeam(
                    TeamID.BLUE,
                    players = mockedPlayers1()
                )
                updateAlly(ally)
                val enemy = PreGameTeam(
                    TeamID.RED,
                    players = mockedPlayers1()
                )
                updateEnemy(enemy)
            }
    }
    return state
}

private fun mockedPlayers1(): ImmutableList<PreGamePlayer> {
    return persistentListOf(
        mockPreGamePlayer(
            "Dex",
            characterId = "chamber_id",
            selectionState = CharacterSelectionState.SELECTED,
            preGamePlayerState = PreGamePlayerState.JOINED,
            competitiveTier = 20,
            incognito = false,
            isCaptain = false
        ),
        mockPreGamePlayer(
            "Dokka",
            characterId = "neon_id",
            selectionState = CharacterSelectionState.LOCKED,
            preGamePlayerState = PreGamePlayerState.JOINED,
            competitiveTier = 21,
            incognito = false,
            isCaptain = false
        ),
        mockPreGamePlayer(
            "Moon",
            characterId = "jett_id",
            selectionState = CharacterSelectionState.LOCKED,
            preGamePlayerState = PreGamePlayerState.JOINED,
            competitiveTier = 22,
            incognito = false,
            isCaptain = false
        ),
        mockPreGamePlayer(
            "Hive",
            characterId = "grenadier_id",
            selectionState = CharacterSelectionState.SELECTED,
            preGamePlayerState = PreGamePlayerState.JOINED,
            competitiveTier = 23,
            incognito = false,
            isCaptain = false
        ),
        mockPreGamePlayer(
            "Lock",
            "",
            selectionState = CharacterSelectionState.NONE,
            preGamePlayerState = PreGamePlayerState.JOINED,
            competitiveTier = 24,
            incognito = true,
            isCaptain = false
        )
    )
}

private fun mockPlayerCardState(
    index: Int,
    name: String,
    nameTag: String,
    incognito: Boolean,
    tier: Int,
    lockedIn: Boolean,
    selectedAgentName: String,
    selectedAgentRoleName: String,
    isUser: Boolean
): AgentSelectionPlayerCardState = AgentSelectionPlayerCardState(
    playerGameName = name.takeIf { !incognito } ?: "Player ${index + 1}",
    playerGameNameTag = nameTag.takeIf { !incognito } ?: "",
    hasSelectedAgent = true,
    selectedAgentName = selectedAgentName,
    selectedAgentIcon = LocalImageData.Resource(agentDisplayIcon(selectedAgentName)),
    selectedAgentIconKey = Unit,
    selectedAgentRoleName = selectedAgentRoleName,
    selectedAgentRoleIcon = LocalImageData.Resource(agentRoleIconForName(selectedAgentRoleName)),
    selectedAgentRoleIconKey = Unit,
    isLockedIn = lockedIn,
    tierName = "",
    tierIcon = LocalImageData.Resource(latestPatchTierIcon(tier)),
    tierIconKey = Unit,
    isUser = isUser
)

private fun playerNameFromMockedPUUID(
    puuid: String
) = puuid

private fun agentNameFromMockedAgentId(
    agentID: String
) = when(agentID) {
    "chamber_id" -> "Chamber"
    "neon_id" -> "Neon"
    "jett_id" -> "Jett"
    "grenadier_id" -> "KAY/O"
    else -> ""
}

private fun agentRoleFromMockedAgentId(
    agentID: String
) = when(agentID) {
    "chamber_id" -> "sentinel"
    "neon_id", "jett_id" -> "duelist"
    "grenadier_id" -> "initiator"
    else -> ""
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
        0, 1, 2 -> R_ASSET.raw.unranked_smallicon
        3 -> R_ASSET.raw.iron1_smallicon
        4 -> R_ASSET.raw.iron2_smallicon
        5 -> R_ASSET.raw.iron3_smallicon
        6 -> R_ASSET.raw.bronze1_smallicon
        7 -> R_ASSET.raw.bronze2_smallicon
        8 -> R_ASSET.raw.bronze3_smallicon
        9 -> R_ASSET.raw.silver1_smallicon
        10 -> R_ASSET.raw.silver2_smallicon
        11 -> R_ASSET.raw.silver3_smallicon
        12-> R_ASSET.raw.gold1_smallicon
        13 -> R_ASSET.raw.gold2_smallicon
        14 -> R_ASSET.raw.gold3_smallicon
        15 -> R_ASSET.raw.platinum1_smallicon
        16 -> R_ASSET.raw.platinum2_smallicon
        17 -> R_ASSET.raw.platinum3_smallicon
        18 -> R_ASSET.raw.diamond1_smallicon
        19 -> R_ASSET.raw.diamond2_smallicon
        20 -> R_ASSET.raw.diamond3_smallicon
        21 -> R_ASSET.raw.ascendant1_smallicon
        22 -> R_ASSET.raw.ascendant2_smallicon
        23 -> R_ASSET.raw.ascendant3_smallicon
        24 -> R_ASSET.raw.immortal1_smallicon
        25 -> R_ASSET.raw.immortal2_smallicon
        26 -> R_ASSET.raw.immortal3_smallicon
        27 -> R_ASSET.raw.radiant_smallicon
        else -> 0
    }
}

private fun agentDisplayIcon(agentName: String): Int {
    // TODO: codename as well
    return when(agentName.lowercase()) {
        "chamber" -> R_ASSET.raw.chamber_displayicon
        "neon" -> R_ASSET.raw.neon_displayicon
        "jett" -> R_ASSET.raw.jett_displayicon
        "kayo", "kay/o", "grenadier" -> R_ASSET.raw.grenadier_displayicon
        else -> 0
    }
}

private fun agentRoleIconForName(name: String): Int {
    return when(name.lowercase()) {
        "controller" -> R_ASSET.raw.role_controller_displayicon
        "duelist" -> R_ASSET.raw.role_duelist_displayicon
        "initiator" -> R_ASSET.raw.role_initiator_displayicon
        "sentinel" -> R_ASSET.raw.role_sentinel_displayicon
        else ->0
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
                Log.d("Draw4PingBar", "n=$n, w=$width, h=$height")
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