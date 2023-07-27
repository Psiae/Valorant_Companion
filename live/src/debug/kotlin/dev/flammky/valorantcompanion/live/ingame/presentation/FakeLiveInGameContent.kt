package dev.flammky.valorantcompanion.live.ingame.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.live.pvp.ingame.presentation.LiveInGameTeamMembersUI
import dev.flammky.valorantcompanion.live.pvp.ingame.presentation.TeamMember
import dev.flammky.valorantcompanion.pvp.TeamID
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun FakeLiveInGameContent(
    modifier: Modifier
) = Column(modifier) {
    FakeLiveInGameTopBar()
    Spacer(modifier = Modifier.height(10.dp))
    LiveInGameTeamMembersUI(
        user = "dokka",
        matchKey = remember { Any() },
        loading = false,
        allyProvided = true,
        enemyProvided = true,
        ally = remember {
            dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam(
                id = TeamID.BLUE,
                members = persistentListOf(
                    TeamMember(
                        "dokka",
                        ValorantAgentIdentity.NEON.uuid,
                        "1",
                        100,
                        false
                    ),
                    TeamMember(
                        "dex",
                        ValorantAgentIdentity.CHAMBER.uuid,
                        "2",
                        101,
                        false
                    ),
                    TeamMember(
                        "moon",
                        ValorantAgentIdentity.JETT.uuid,
                        "3",
                        102,
                        false
                    ),
                    TeamMember(
                        "hive",
                        ValorantAgentIdentity.KAYO.uuid,
                        "4",
                        103,
                        false
                    ),
                    TeamMember(
                        "lock",
                        ValorantAgentIdentity.OMEN.uuid,
                        "5",
                        104,
                        false
                    )
                )
            )
        },
        enemy = remember {
            dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam(
                id = TeamID.BLUE,
                members = persistentListOf(
                    TeamMember(
                        "lock",
                        ValorantAgentIdentity.OMEN.uuid,
                        "5",
                        104,
                        false
                    ),
                    TeamMember(
                        "hive",
                        ValorantAgentIdentity.KAYO.uuid,
                        "4",
                        103,
                        false
                    ),
                    TeamMember(
                        "moon",
                        ValorantAgentIdentity.JETT.uuid,
                        "3",
                        102,
                        false
                    ),
                    TeamMember(
                        "dex",
                        ValorantAgentIdentity.CHAMBER.uuid,
                        "2",
                        101,
                        false
                    ),
                    TeamMember(
                        "dokka",
                        ValorantAgentIdentity.NEON.uuid,
                        "1",
                        100,
                        false
                    ),
                )
            )
        }
    )
}