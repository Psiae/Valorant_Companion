package dev.flammky.valorantcompanion.live.ingame.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark

@Composable
internal fun LiveInGameTeamMembersColumn(
    modifier: Modifier,
    matchKey: Any,
    user: String,
    loading: Boolean,
    membersProvided: Boolean,
    getMembers: () -> List<TeamMember>
) {
    val childModifier = modifier then remember {
        Modifier
            .pointerInput(Unit) {}
            .padding(horizontal = 10.dp)
    }
    if (membersProvided) {
        LiveInGameTeamMembersColumn(
            modifier = childModifier ,
            members = getMembers().map { member ->
                rememberLiveInGameTeamMemberCardPresenter().present(
                    matchKey = matchKey,
                    user = user,
                    id = member.puuid,
                    playerAgentID = member.agentID,
                    playerCardID = member.playerCardID,
                    accountLevel = member.accountLevel,
                    incognito = member.incognito
                )
            }
        )
    } else {
        EmptyLiveInGameTeamMembersColumn(
            modifier = childModifier,
            loading = loading
        )
    }
}

@Composable
private fun LiveInGameTeamMembersColumn(
    modifier: Modifier,
    members: List<LiveInGameTeamMemberCardState>
) = Column(
    modifier = modifier,
    verticalArrangement = remember { Arrangement.spacedBy(5.dp) },
    content = {
        members.forEach { member ->
            LiveInGameTeamMemberCard(
                modifier = Modifier,
                state = member
            )
        }
    }
)

@Composable
private fun EmptyLiveInGameTeamMembersColumn(
    modifier: Modifier,
    loading: Boolean
) = Box(modifier = modifier.fillMaxWidth()) {
    if (!loading) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "THIS TEAM MEMBERS WERE NOT PROVIDED BY THE ENDPOINT",
            color = LocalIsThemeDark.current.let { dark ->
                remember(dark) { (if (dark) Color.White else Color.Black).copy(alpha = 0.38f) }
            },
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}