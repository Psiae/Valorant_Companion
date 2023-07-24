package dev.flammky.valorantcompanion.live.pvp.party.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundContentColorAsState
import dev.flammky.valorantcompanion.base.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration

@Composable
fun LivePartyMatchmakingColumn(
    state: LivePartyMatchmakingColumnState
) = Column(modifier = Modifier.fillMaxWidth()) {

    QueueIdDropDown(
        canModify = state.isUserOwner,
        currentQueueID = state.queueID ?: PlayerPartyData.UNSET.matchmakingQueueID,
        eligibles = state.eligibleQueues ?: PlayerPartyData.UNSET.eligible,
        changeQueue = run {
            val partyID = state.partyID ?: return@run {}
            { qID -> state.changeMatchmakingQueue(partyID, qID) }
        }
    )

    Spacer(modifier = Modifier.height(4.dp))

    if (state.inQueue) {
        InMatchmakingButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            state = rememberInMatchmakingButtonPresenter()
                .run {
                    val isUserOwner = state.isUserOwner
                    val partyID = state.partyID
                    present(
                        isUserOwner = isUserOwner,
                        timeStamp = state.timeStamp ?: Duration.ZERO,
                        cancelMatchmakingKey = remember(state, partyID) { Any() },
                        cancelMatchmaking = run {
                            if (partyID == null) return@run {}
                            { state.quitMatchmakingQueue(partyID)}
                        }
                    )
                }
        )
    } else {
        EnterMatchmakingButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            state.isUserOwner,
            state.members ?: persistentListOf(),
            enterMatchmaking = run {
                val partyID = state.partyID ?: return@run {}
                { state.enterMatchmakingQueue(partyID) }
            }
        )
    }
}

@Composable
private fun QueueIdDropDown(
    canModify: Boolean,
    currentQueueID: String,
    eligibles: List<String>,
    changeQueue: (String) -> Unit
) {
    val expandedState = remember(canModify) {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .clickable(canModify) { expandedState.value = !expandedState.value }
            .padding(vertical = 2.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = parseAndNormalizeMatchmakingIdToUiString(currentQueueID).uppercase(),
                color = Material3Theme.backgroundContentColorAsState().value
            )
            Spacer(Modifier.width(2.dp))
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = R.drawable.arrow_drop_down_48px),
                tint = Material3Theme.backgroundContentColorAsState().value,
                contentDescription = "game mode"
            )
            DropdownMenu(
                modifier = Modifier.background(
                    if (LocalIsThemeDark.current) {
                        Color(0xFF303030)
                    } else {
                        Color.White
                    }
                ),
                expanded = expandedState.value,
                onDismissRequest = { expandedState.value = false },
            ) {
                run {
                    val enabled = eligibles.any { it.equals("unrated", true) }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "UNRATED",
                                color = if (LocalIsThemeDark.current) {
                                    Color.White
                                } else {
                                    Color.Black
                                }.let {
                                    if (enabled) it else it.copy(alpha = 0.38f)
                                }
                            )
                        },
                        enabled = enabled,
                        onClick = { changeQueue("unrated") }
                    )
                }
                run {
                    val enabled = eligibles.any { it.equals("competitive", true) }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "COMPETITIVE",
                                color = if (LocalIsThemeDark.current) {
                                    Color.White
                                } else {
                                    Color.Black
                                }.let {
                                    if (enabled) it else it.copy(alpha = 0.38f)
                                }
                            )
                        },
                        enabled = eligibles.any { it.equals("competitive", true) },
                        onClick = { changeQueue("competitive") }
                    )
                }
                run {
                    val enabled = eligibles.any { it.equals("swiftplay", true) }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "SWIFTPLAY",
                                color = if (LocalIsThemeDark.current) {
                                    Color.White
                                } else {
                                    Color.Black
                                }.let {
                                    if (enabled) it else it.copy(alpha = 0.38f)
                                }
                            )
                        },
                        enabled = enabled,
                        onClick = { changeQueue("swiftplay") }
                    )
                }
                run {
                    val enabled = eligibles.any { it.equals("spikerush", true) }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "SPIKE RUSH",
                                color = if (LocalIsThemeDark.current) {
                                    Color.White
                                } else {
                                    Color.Black
                                }.let {
                                    if (enabled) it else it.copy(alpha = 0.38f)
                                }
                            )
                        },
                        enabled = enabled,
                        onClick = { changeQueue("spikerush") }
                    )
                }
                run {
                    val enabled = eligibles.any { it.equals("deathmatch", true) }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "DEATHMATCH",
                                color = if (LocalIsThemeDark.current) {
                                    Color.White
                                } else {
                                    Color.Black
                                }.let {
                                    if (enabled) it else it.copy(alpha = 0.38f)
                                }
                            )
                        },
                        enabled = enabled,
                        onClick = { changeQueue("deathmatch") }
                    )
                }
                run {
                    val enabled = eligibles.any { it.equals("ggteam", true) }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "ESCALATION",
                                color = if (LocalIsThemeDark.current) {
                                    Color.White
                                } else {
                                    Color.Black
                                }.let {
                                    if (enabled) it else it.copy(alpha = 0.38f)
                                }
                            )
                        },
                        enabled = enabled,
                        onClick = { changeQueue("ggteam") }
                    )
                }
                run {
                    val enabled = eligibles.any { it.equals("hurm", true) }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "TEAM DEATHMATCH",
                                color = if (LocalIsThemeDark.current) {
                                    Color.White
                                } else {
                                    Color.Black
                                }.let {
                                    if (enabled) it else it.copy(alpha = 0.38f)
                                }
                            )
                        },
                        enabled = enabled,
                        onClick = { changeQueue("hurm") }
                    )
                }
            }
        }
    }
}

@Composable
fun InMatchmakingButton(
    modifier: Modifier,
    isUserOwner: Boolean,
) {

}

@Composable
fun EnterMatchmakingButton(
    modifier: Modifier,
    isUserOwner: Boolean,
    members: List<PlayerPartyMemberInfo>,
    enterMatchmaking: () -> Unit
) {
    val allPartyMembersReady =
        if (members is ImmutableList<*>) {
            remember(members) {
                members.isNotEmpty() && members.all { it.isReady }
            }
        } else {
            members.isNotEmpty() && members.all { it.isReady }
        }
    EnterMatchmakingButton(
        modifier = modifier,
        enabled = isUserOwner && allPartyMembersReady,
        disabledMessage = when {
            !isUserOwner -> null
            !allPartyMembersReady -> "Party Not Ready"
            else -> null
        },
        enterMatchmaking = enterMatchmaking
    )
}

@Composable
fun EnterMatchmakingButton(
    modifier: Modifier,
    enabled: Boolean,
    disabledMessage: String?,
    enterMatchmaking: () -> Unit
) {
    Row(
        modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            modifier = Modifier
                .heightIn(max = 34.dp),
            contentPadding = PaddingValues(
                vertical = 5.dp,
                horizontal = 16.dp
            ),
            enabled = enabled,
            onClick = enterMatchmaking,
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color(0xFFF4444C),
                contentColor = Color.White,
            )
        ) {
            if (enabled) {
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = "START",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            } else {
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = disabledMessage ?: "START",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White.copy(alpha = 0.38f)
                )
            }
        }
    }
}


private fun parseAndNormalizeMatchmakingIdToUiString(id: String?): String {
    if (id == null) return ""
    return when (id.lowercase()) {
        "ggteam" -> "escalation"
        "spikerush" -> "spike rush"
        "hurm" -> "team deathmatch"
        else -> id
    }
}