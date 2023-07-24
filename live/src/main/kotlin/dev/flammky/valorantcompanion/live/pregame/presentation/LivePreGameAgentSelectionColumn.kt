package dev.flammky.valorantcompanion.live.pregame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.di.compose.inject
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgent
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import dev.flammky.valorantcompanion.pvp.pregame.PreGameService
import dev.flammky.valorantcompanion.pvp.pregame.onSuccess
import kotlinx.coroutines.*

@Composable
internal fun LivePreGameAgentSelectionColumn(
    modifier: Modifier = Modifier,
    user: PreGamePlayer,
    ally: PreGameTeam,
    allyKey: Any,
    matchID: String,
    selectAgent: (String) -> Unit,
    lockAgent: (String) -> Unit
) = Column(modifier = modifier) {
    ally.players.forEachIndexed { _, player ->
        AgentSelectionPlayerCard(
            state = rememberAgentSelectionPlayerCardPresenter().present(
                user = user.puuid,
                player = player,
                matchID = matchID
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
    Spacer(modifier = Modifier.height(15.dp))
    LivePreGameAgentSelectionColumnLockInButton(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        hasSelectedAgent = user.characterSelectionState.isSelectedOrLocked,
        agentName = remember(user.characterID) {
            ValorantAgentIdentity.ofID(user.characterID)?.displayName ?: ""
        },
        alreadyLocked = user.characterSelectionState.isLocked,
        lock = { lockAgent(user.characterID) }
    )
    Spacer(modifier = Modifier.height(15.dp))
    LivePreGameAgentSelectionColumnAgentPool(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        user = user,
        playersKey = allyKey,
        players = ally.players,
        selectAgent = selectAgent
    )
    Spacer(modifier = Modifier.height(15.dp))
}

@Composable
private fun LivePreGameAgentSelectionColumnLockInButton(
    modifier: Modifier,
    hasSelectedAgent: Boolean,
    agentName: String,
    alreadyLocked: Boolean,
    lock: () -> Unit
) {
    if (!hasSelectedAgent) {
        Text(
            modifier = modifier,
            text = "SELECT AN AGENT",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = (if (LocalIsThemeDark.current) Color.White else Color.Black).copy(alpha = 0.38f)
        )
    } else if (alreadyLocked) {
        Text(
            modifier = modifier,
            text ="LOCKED $agentName".uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = (if (LocalIsThemeDark.current) Color.White else Color.Black).copy(alpha = 0.38f)
        )
    } else {
        Box(
            modifier = modifier then remember(lock) {
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFF4444C))
                    .clickable(onClick = lock)
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            }
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "LOCK $agentName".uppercase(),
                style = MaterialTheme
                    .typography
                    .labelLarge
                    .copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
        }
    }
}

@Composable
private fun LivePreGameAgentSelectionColumnAgentPool(
    modifier: Modifier,
    user: PreGamePlayer,
    playersKey: Any,
    players: List<PreGamePlayer>,
    selectAgent: (String) -> Unit,
) {
    val userID = user.puuid
    val unlockedAgents = run {
        val preGameService: PreGameService = inject()
        val returns = remember(userID) { mutableStateOf<List<String>>(emptyList()) }
        DisposableEffect(
            key1 = userID,
            effect = {
                val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
                val client = preGameService.createUserClient(userID)
                coroutineScope.launch {
                    val def = client.fetchUnlockedAgentsAsync()
                    runCatching { def.await() }
                        .onFailure { def.cancel() }
                        .onSuccess { fetchResult ->
                            fetchResult.onSuccess { agents ->
                                returns.value = (agents.map { it.uuid })
                            }
                        }
                }

                onDispose { coroutineScope.cancel() ; client.dispose() }
            }
        )
        returns.value
    }

    val disabledAgents = remember(playersKey) {
        players.filter { it.characterSelectionState.isLocked }.map { it.characterID }
    }

    AgentSelectionPool(
        modifier = modifier,
        unlockedAgents = unlockedAgents,
        disabledAgents = disabledAgents,
        agentPool = ValorantAgent.asList(),
        selectedAgent = user.characterID,
        onSelected = selectAgent
    )
}
