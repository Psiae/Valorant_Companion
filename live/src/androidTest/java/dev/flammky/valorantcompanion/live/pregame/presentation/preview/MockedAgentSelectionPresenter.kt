package dev.flammky.valorantcompanion.live.pregame.presentation.preview

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.flammky.valorantcompanion.live.pregame.presentation.*
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun rememberMockedAgentSelectionPresenter(): MockedAgentSelectionPresenter {
    return remember {
        MockedAgentSelectionPresenter()
    }
}

class MockedAgentSelectionPresenter() {

    @Composable
    fun present(): AgentSelectionState {
        val upAlly = remember {
            mutableStateOf(AllyTeam.mock())
        }
        val upEnemy = remember {
            mutableStateOf(EnemyTeam.mock())
        }
        return run {
            val ally = upAlly.value
            val enemy = upEnemy.value
            val user = ally.p2
            AgentSelectionState(
                ally = ally.toPreGameTeam(),
                enemy = enemy.toPreGameTeam(),
                user = user,
                selectAgent = { id ->
                    val c = upAlly.value
                    if (c.p2.characterSelectionState == CharacterSelectionState.LOCKED) return@AgentSelectionState
                    upAlly.value = c.copy(p2 = c.p2.copy(characterID = id))
                },
                lockIn = {
                    val c = upAlly.value
                    upAlly.value = c.copy(p2 = c.p2.copy(characterSelectionState = CharacterSelectionState.LOCKED))
                }
            )
        }
    }


    private class AllyTeam(
        val teamID: TeamID,
        val p1: PreGamePlayer,
        val p2: PreGamePlayer,
        val p3: PreGamePlayer,
        val p4: PreGamePlayer,
        val p5: PreGamePlayer
    ) {
        fun toPreGameTeam() = PreGameTeam(
            teamID,
            persistentListOf(p1, p2, p3, p4, p5)
        )

        fun copy(
            teamID: TeamID = this.teamID,
            p1: PreGamePlayer = this.p1,
            p2: PreGamePlayer = this.p2,
            p3: PreGamePlayer = this.p3,
            p4: PreGamePlayer = this.p4,
            p5: PreGamePlayer = this.p5
        ) = AllyTeam(
            teamID,
            p1,
            p2,
            p3,
            p4,
            p5
        )

        companion object {
            fun mock() : AllyTeam {
                return AllyTeam(
                    TeamID.BLUE,
                    PreGamePlayer(
                        puuid = "Dex",
                        characterID = ValorantAgentIdentity.CHAMBER.uuid,
                        characterSelectionState = CharacterSelectionState.SELECTED,
                        pregamePlayerState = PreGamePlayerState.JOINED,
                        competitiveTier = 20,
                        identity = PreGamePlayerInfo("Dex", "", "", 100, "", false, false),
                        seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
                        isCaptain = true
                    ),
                    p2 = PreGamePlayer(
                        puuid = "Dokka",
                        characterID = ValorantAgentIdentity.NEON.uuid,
                        characterSelectionState = CharacterSelectionState.SELECTED,
                        pregamePlayerState = PreGamePlayerState.JOINED,
                        competitiveTier = 21,
                        identity = PreGamePlayerInfo("Dokka", "", "", 100, "", false, false),
                        seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
                        isCaptain = false
                    ),
                    p3 = PreGamePlayer(
                        puuid = "Moon",
                        characterID = ValorantAgentIdentity.JETT.uuid,
                        characterSelectionState = CharacterSelectionState.LOCKED,
                        pregamePlayerState = PreGamePlayerState.JOINED,
                        competitiveTier = 22,
                        identity = PreGamePlayerInfo("Moon", "", "", 100, "", false, false),
                        seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
                        isCaptain = false
                    ),
                    p4 = PreGamePlayer(
                        puuid = "Hive",
                        characterID = ValorantAgentIdentity.KAYO.uuid,
                        characterSelectionState = CharacterSelectionState.SELECTED,
                        pregamePlayerState = PreGamePlayerState.JOINED,
                        competitiveTier = 23,
                        identity = PreGamePlayerInfo("Hive", "", "", 100, "", false, false),
                        seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
                        isCaptain = false
                    ),
                    p5 = PreGamePlayer(
                        puuid = "Lock",
                        characterID = "",
                        characterSelectionState = CharacterSelectionState.NONE,
                        pregamePlayerState = PreGamePlayerState.JOINED,
                        competitiveTier = 24,
                        identity = PreGamePlayerInfo("Lock", "", "", 100, "", false, false),
                        seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
                        isCaptain = false
                    )
                )
            }
        }
    }

    private class EnemyTeam(
        val teamID: TeamID,
        val p1: PreGamePlayer,
        val p2: PreGamePlayer,
        val p3: PreGamePlayer,
        val p4: PreGamePlayer,
        val p5: PreGamePlayer
    ) {
        fun toPreGameTeam() = PreGameTeam(
            teamID,
            persistentListOf(p1, p2, p3, p4, p5)
        )

        companion object {
            fun mock(): EnemyTeam {
                return EnemyTeam(
                    TeamID.BLUE,
                    PreGamePlayer(
                        puuid = "Dex",
                        characterID = ValorantAgentIdentity.CHAMBER.uuid,
                        characterSelectionState = CharacterSelectionState.SELECTED,
                        pregamePlayerState = PreGamePlayerState.JOINED,
                        competitiveTier = 20,
                        identity = PreGamePlayerInfo("Dex", "", "", 100, "", false, false),
                        seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
                        isCaptain = true
                    ),
                    p2 = PreGamePlayer(
                        puuid = "Dokka",
                        characterID = ValorantAgentIdentity.NEON.uuid,
                        characterSelectionState = CharacterSelectionState.LOCKED,
                        pregamePlayerState = PreGamePlayerState.JOINED,
                        competitiveTier = 21,
                        identity = PreGamePlayerInfo("Dokka", "", "", 100, "", false, false),
                        seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
                        isCaptain = false
                    ),
                    p3 = PreGamePlayer(
                        puuid = "Moon",
                        characterID = ValorantAgentIdentity.JETT.uuid,
                        characterSelectionState = CharacterSelectionState.LOCKED,
                        pregamePlayerState = PreGamePlayerState.JOINED,
                        competitiveTier = 22,
                        identity = PreGamePlayerInfo("Moon", "", "", 100, "", false, false),
                        seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
                        isCaptain = false
                    ),
                    p4 = PreGamePlayer(
                        puuid = "Hive",
                        characterID = ValorantAgentIdentity.NEON.uuid,
                        characterSelectionState = CharacterSelectionState.LOCKED,
                        pregamePlayerState = PreGamePlayerState.JOINED,
                        competitiveTier = 23,
                        identity = PreGamePlayerInfo("Hive", "", "", 100, "", false, false),
                        seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
                        isCaptain = false
                    ),
                    p5 = PreGamePlayer(
                        puuid = "Lock",
                        characterID = ValorantAgentIdentity.NEON.uuid,
                        characterSelectionState = CharacterSelectionState.LOCKED,
                        pregamePlayerState = PreGamePlayerState.JOINED,
                        competitiveTier = 24,
                        identity = PreGamePlayerInfo("Lock", "", "", 100, "", false, false),
                        seasonalBadgeInfo = SeasonalBadgeInfo("", 100, null, 0, 0),
                        isCaptain = false
                    )
                )
            }
        }
    }
}