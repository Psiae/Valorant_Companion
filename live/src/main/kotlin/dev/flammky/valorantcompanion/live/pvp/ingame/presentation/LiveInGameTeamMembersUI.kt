package dev.flammky.valorantcompanion.live.pvp.ingame.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.flammky.valorantcompanion.base.rememberThis

@Composable
fun LiveInGameTeamMembersUI(
    user: String,
    matchKey: Any,
    loading: Boolean,
    allyProvided: Boolean,
    enemyProvided: Boolean,
    ally: dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam?                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       ?,
    enemy: dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam?
) {
    val targetAlly = remember {
        mutableStateOf(true)
    }
    Column {
        LiveInGameTeamTab(
            modifier = Modifier,
            allySelected = targetAlly.value,
            selectAlly = remember {
                block@ {
                    if (targetAlly.value) return@block
                    targetAlly.value = true
                }
            },
            selectEnemy = remember {
                block@ {
                    if (!targetAlly.value) return@block
                    targetAlly.value = false
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box {
            LiveInGameTeamMembersColumn(
                modifier = Modifier.rememberThis(targetAlly.value) {
                    zIndex(if (targetAlly.value) 1f else 0f).alpha(if (targetAlly.value) 1f else 0f)
                },
                matchKey = matchKey,
                user = user,
                loading = loading,
                membersProvided = allyProvided,
                getMembers = { ally?.members ?: emptyList() }
            )
            LiveInGameTeamMembersColumn(
                modifier = Modifier.rememberThis(targetAlly.value) {
                    zIndex(if (targetAlly.value) 0f else 1f).alpha(if (targetAlly.value) 0f else 1f)
                },
                matchKey = matchKey,
                user = user,
                loading = loading,
                membersProvided = enemyProvided,
                getMembers = { enemy?.members ?: emptyList() }
            )
        }
    }
}