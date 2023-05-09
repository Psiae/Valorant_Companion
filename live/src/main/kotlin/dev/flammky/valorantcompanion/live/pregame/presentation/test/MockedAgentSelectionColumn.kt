package dev.flammky.valorantcompanion.live.pregame.presentation.test

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.surfaceColorAsState
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
private fun AgentSelectionColumnPreview(
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
                Spacer(modifier = Modifier.height(10.dp))
                mockedAgentSelectionColumnState().ally?.players?.forEachIndexed { i, player ->
                    key(player.puuid) {
                        val name = playerNameFromMockedPUUID(player.puuid)
                        AgentSelectionPlayerCard(
                            state = mockPlayerCardState(
                                index = i,
                                name = name,
                                nameTag = "0000",
                                incognito = player.identity.incognito,
                                tier = player.competitiveTier,
                                lockedIn =  player.characterSelectionState == CharacterSelectionState.LOCKED,
                                selectedAgentName = agentNameFromMockedAgentId(player.characterID),
                                isUser = name == "Dokka"
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
        playerWithChamber(CharacterSelectionState.SELECTED),
        playerWithNeon(CharacterSelectionState.LOCKED),
        playerWithJett(CharacterSelectionState.LOCKED),
        playerWithKayo(CharacterSelectionState.SELECTED),
        // typical smoke filler
        playerWithUnselectedAgent()
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
    isUser: Boolean
): AgentSelectionPlayerCardState = AgentSelectionPlayerCardState(
    playerGameName = name.takeIf { !incognito } ?: "Player ${index + 1}",
    playerGameNameTag = nameTag.takeIf { !incognito } ?: "",
    hasSelectedAgent = true,
    selectedAgentName = selectedAgentName,
    selectedAgentIcon = LocalImageData.Resource(agentDisplayIcon(selectedAgentName)),
    selectedAgentIconKey = Unit,
    isLockedIn = lockedIn,
    tierName = "",
    tierIcon = LocalImageData.Resource(latestPatchTierIcon(tier)),
    tierIconKey = Unit,
    isUser = isUser
)

private fun playerNameFromMockedPUUID(
    puuid: String
) = when(puuid) {
    "player_with_chamber" -> "Dex"
    "player_with_neon" -> "Dokka"
    "player_with_jett" -> "Moon"
    "player_with_kayo" -> "Hive"
    else -> ""
}

private fun agentNameFromMockedAgentId(
    agentID: String
) = when(agentID) {
    "chamber_id" -> "Chamber"
    "neon_id" -> "Neon"
    "jett_id" -> "Jett"
    "grenadier_id" -> "KAY/O"
    else -> ""
}


private fun playerWithChamber(
    selectionState: CharacterSelectionState
): PreGamePlayer = PreGamePlayer(
    puuid = "player_with_chamber",
    characterID = "chamber_id",
    characterSelectionState = selectionState,
    pregamePlayerState = PreGamePlayerState.JOINED,
    competitiveTier = 20,
    identity = PreGamePlayerInfo("player_with_chamber", "", "", 100, "", false, false),
    seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
    isCaptain = true
)

private fun playerWithNeon(
    selectionState: CharacterSelectionState
): PreGamePlayer = PreGamePlayer(
    puuid = "player_with_neon",
    characterID = "neon_id",
    characterSelectionState = selectionState,
    pregamePlayerState = PreGamePlayerState.JOINED,
    competitiveTier = 21,
    identity = PreGamePlayerInfo("player_with_neon", "", "", 100, "", false, false),
    seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
    isCaptain = true
)

private fun playerWithJett(
    selectionState: CharacterSelectionState
): PreGamePlayer = PreGamePlayer(
    puuid = "player_with_jett",
    characterID = "jett_id",
    characterSelectionState = selectionState,
    pregamePlayerState = PreGamePlayerState.JOINED,
    competitiveTier = 22,
    identity = PreGamePlayerInfo("player_with_jett", "", "", 100, "", false, false),
    seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
    isCaptain = true
)

private fun playerWithKayo(
    selectionState: CharacterSelectionState
): PreGamePlayer = PreGamePlayer(
    puuid = "player_with_kayo",
    characterID = "grenadier_id",
    characterSelectionState = selectionState,
    pregamePlayerState = PreGamePlayerState.JOINED,
    competitiveTier = 23,
    identity = PreGamePlayerInfo("player_with_kayo", "", "", 100, "", false, false),
    seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
    isCaptain = true
)



private fun playerWithViper(
    selectionState: CharacterSelectionState
): PreGamePlayer = PreGamePlayer(
    puuid = "player_with_viper",
    characterID = "viper_id",
    characterSelectionState = selectionState,
    pregamePlayerState = PreGamePlayerState.JOINED,
    competitiveTier = 25,
    identity = PreGamePlayerInfo("player_with_viper", "", "", 100, "", false, false),
    seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
    isCaptain = true
)

private fun playerWithUnselectedAgent(

): PreGamePlayer = PreGamePlayer(
    puuid = "player_with_no_selected",
    characterID = "",
    characterSelectionState = CharacterSelectionState.NONE,
    pregamePlayerState = PreGamePlayerState.JOINED,
    competitiveTier = 24,
    identity = PreGamePlayerInfo("player_with_no_selected", "", "", 100, "", false, false),
    seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
    isCaptain = true
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