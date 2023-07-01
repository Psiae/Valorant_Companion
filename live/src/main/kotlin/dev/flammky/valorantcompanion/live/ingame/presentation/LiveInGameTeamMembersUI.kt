package dev.flammky.valorantcompanion.live.ingame.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.flammky.valorantcompanion.base.runRemember

@Composable
fun LiveInGameTeamMembersUI(
    user: String,
    matchKey: Any,
    ally: InGameTeam                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       ?,
    enemy: InGameTeam?
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
                modifier = Modifier.runRemember(targetAlly.value) {
                    zIndex(if (targetAlly.value) 1f else 0f)
                },
                matchKey = matchKey,
                user = user,
                members = ally?.members ?: emptyList()
            )
            LiveInGameTeamMembersColumn(
                modifier = Modifier.runRemember(targetAlly.value) {
                    zIndex(if (targetAlly.value) 0f else 1f)
                },
                matchKey = matchKey,
                user = user,
                members = enemy?.members ?: emptyList(),
            )
        }
    }
}