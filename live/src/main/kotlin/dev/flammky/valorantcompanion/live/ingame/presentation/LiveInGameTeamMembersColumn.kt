package dev.flammky.valorantcompanion.live.ingame.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun LiveInGameTeamMembersColumn(
    modifier: Modifier,
    matchKey: Any,
    user: String,
    members: List<TeamMember>
) = Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
    members.forEach { member ->
        LiveInGameTeamMemberCard(
            modifier = Modifier.padding(horizontal = 5.dp),
            state = rememberLiveInGameTeamMemberCardPresenter().present(
                matchKey = matchKey,
                user = user,
                id = member.puuid,
                playerAgentID = member.agentID,
                playerCardID = member.playerCardID,
                accountLevel = member.accountLevel,
                incognito = member.incognito
            )
        )
    }
}