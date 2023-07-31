package dev.flammky.valorantcompanion.live.pvp.party.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.assets.player_card.PlayerCardArtType
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.player_card.LoadPlayerCardRequest
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundContentColorAsState
import dev.flammky.valorantcompanion.live.pingStrengthInRangeOf4
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get as getFromKoin

@Composable
fun rememberPartyColumnMemberCardPresenter(
    assetsService: ValorantAssetsService = getFromKoin()
): PartyColumnMemberCardPresenter {
    return remember(assetsService) { PartyColumnMemberCardPresenter(assetsService) }
}

class PartyColumnMemberCardPresenter(
    private val assetsService: ValorantAssetsService,
) {

    @Composable
    inline fun present(
        memberinfo: PlayerPartyMemberInfo,
        memberName: Result<PlayerPartyMemberName>?,
        partyPreferredPods: List<String>,
        changePreferredPodsKey: Any,
        noinline changePreferredPods: (Set<String>) -> Unit
    ): PartyColumnMemberCardState {
        return present(
            puuid = memberinfo.puuid,
            name = memberName?.getOrNull()?.name ?: "",
            tag = memberName?.getOrNull()?.tag ?: "",
            playerCardId = memberinfo.cardArtId,
            isLeader = memberinfo.isLeader,
            isReady = memberinfo.isReady,
            pods = memberinfo.gamePodData,
            preferredPods = partyPreferredPods,
            changePreferredPodsKey = changePreferredPodsKey,
            changePreferredPods = changePreferredPods
        )
    }

    @Composable
    fun present(
        puuid: String?,
        name: String?,
        tag: String?,
        playerCardId: String?,
        isLeader: Boolean?,
        isReady: Boolean?,
        pods: List<GamePodConnection>,
        preferredPods: List<String>,
        changePreferredPodsKey: Any,
        changePreferredPods: (Set<String>) -> Unit
    ): PartyColumnMemberCardState {
        Log.d("PartyColumnMemberCardPresenter", "present($puuid, $name, $tag, $playerCardId)")
        val returns = remember(puuid) {
            mutableStateOf(
                PartyColumnMemberCardState(
                    null,
                    null,
                    null,
                    null,
                    null,
                    emptyList(),
                    emptyList(),
                    {  }
                )
            )
        }.apply {
            value = value.copy(
                name = name,
                tag = tag,
                isLeader = isLeader,
                isReady = isReady,
                pods = pods,
                preferredPods = preferredPods,
                changePreferredPods = remember(changePreferredPodsKey) { changePreferredPods }
            )
        }
        val coroutineScope = rememberCoroutineScope()
        DisposableEffect(
            puuid, playerCardId
        ) {
            val supervisor = SupervisorJob()
            val assetClient = assetsService.createLoaderClient()
            coroutineScope.launch(supervisor) {
                playerCardId?.let { id ->
                    runCatching {
                        assetClient
                            .loadUserPlayerCardAsync(LoadPlayerCardRequest(id, PlayerCardArtType.SMALL))
                            .await()
                            .getOrThrow()
                    }.onSuccess {
                        returns.value = returns.value.copy(playerCard = it.value)
                    }
                }
            }
            onDispose {
                supervisor.cancel()
                assetClient.dispose()
            }
        }
        return returns.value
    }
}

data class PartyColumnMemberCardState(
    val playerCard: Any?,
    val name: String?,
    val tag: String?,
    val isLeader: Boolean?,
    val isReady: Boolean?,
    val pods: List<GamePodConnection>,
    val preferredPods: List<String>,
    val changePreferredPods: (Set<String>) -> Unit
) {
}

@Composable
fun PartyColumnMemberCard(
    modifier: Modifier,
    state: PartyColumnMemberCardState
) {
    PlayerCard(
        modifier,
        state.playerCard,
        state.name ?: "",
        state.tag ?: "",
        state.isLeader ?: false,
        state.isReady ?: false,
        state.pods,
        state.preferredPods,
        addPreferredPod = { pod ->
            state.changePreferredPods(
                buildSet {
                    state.preferredPods.forEach { add(it) }
                    add(pod)
                }
            )
        },
        removePreferredPod = { pod ->
            state.changePreferredPods(
                buildSet {
                    state.preferredPods.forEach { if (it != pod) add(it) }
                }
            )
        }
    )
}


@Composable
private fun PlayerCard(
    modifier: Modifier,
    playerCard: Any?,
    name: String,
    tag: String,
    isLeader: Boolean,
    isReady: Boolean,
    pods: List<GamePodConnection>,
    preferredPods: List<String>,
    addPreferredPod: (String) -> Unit,
    removePreferredPod: (String) -> Unit
) {
    val ctx = LocalContext.current
    Row(modifier, horizontalArrangement = Arrangement.SpaceBetween) {

        Row(modifier = Modifier.weight(2f)) {
            Box(modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)) {
                AsyncImage(
                    modifier = Modifier.align(Alignment.Center),
                    model = remember(ctx, playerCard) {
                        ImageRequest.Builder(ctx)
                            .crossfade(true)
                            .data(playerCard)
                            .build()
                    },
                    contentDescription = "player card"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(modifier = Modifier.weight(2f)) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = name,
                        color = Material3Theme.backgroundContentColorAsState().value,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .background(
                                if (LocalIsThemeDark.current) {
                                    Color.White.copy(alpha = 0.1f)
                                } else {
                                    Color.Black.copy(alpha = 0.1f)
                                }
                            )
                    ) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "#$tag",
                            color = Material3Theme.backgroundContentColorAsState().value,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    if (isLeader) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .background(
                                    if (LocalIsThemeDark.current) {
                                        Color.White.copy(alpha = 0.1f)
                                    } else {
                                        Color.Black.copy(alpha = 0.1f)
                                    }
                                )
                        ) {
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Leader",
                                color = Material3Theme.backgroundContentColorAsState().value,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                    }
                }
                Text(
                    text = if (isReady) "Ready" else "Not Ready",
                    color = Material3Theme.backgroundContentColorAsState().value,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        val expandedState = remember { mutableStateOf(false) }
        Row(modifier = Modifier.fillMaxHeight()) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.CenterVertically)
                    .clickable { expandedState.value = true }
            ) {
                Draw4PingBarFromPartyMemberPods(
                    modifier = Modifier
                        .height(12.dp)
                        .width(24.dp)
                        .align(Alignment.Center),
                    memberPods = pods,
                    partyPreferredPods = preferredPods
                )
            }
            DropdownMenu(
                modifier = Modifier.background(
                    if (LocalIsThemeDark.current) {
                        Color(0xFF303030)
                    } else {
                        Color.White
                    }
                ),
                expanded = expandedState.value,
                onDismissRequest = { expandedState.value = false }
            ) {
                // TODO: show party preferred pods visual
                remember(pods) { pods.sortedBy { it.ping } }.forEach { pod ->
                    key(pod.id) {
                        val preferred = pod.id in preferredPods
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = remember(pod.id) { parsePodName(pod.id) },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (LocalIsThemeDark.current) {
                                        Color.White
                                    } else {
                                        Color.Black
                                    }
                                )
                            },
                            leadingIcon = {
                                Checkbox(
                                    modifier = Modifier.size(24.dp),
                                    checked = preferred,
                                    onCheckedChange = { checked ->
                                        check(checked != preferred)
                                        if (checked)
                                            addPreferredPod(pod.id)
                                        else
                                            removePreferredPod(pod.id)
                                    },
                                    enabled = isLeader
                                )
                            },
                            trailingIcon = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${pod.ping}ms",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (LocalIsThemeDark.current) {
                                            Color.White
                                        } else {
                                            Color.Black
                                        }
                                    )
                                    Draw4PingBar(
                                        modifier = Modifier
                                            .height(12.dp)
                                            .width(24.dp),
                                        pingMs = pod.ping
                                    )
                                }
                            },
                            onClick = {
                                if (preferred)
                                    removePreferredPod(pod.id)
                                else
                                    addPreferredPod(pod.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun parsePodName(id: String): String {
    if (id.isBlank()) return ""
    val split = id.split("-")
    val take = if (split.size > 2) { split.takeLast(2) } else split
    return take.joinToString("-")
}

@Composable
private fun Draw4PingBarFromPartyMemberPods(
    modifier: Modifier,
    memberPods: List<GamePodConnection>,
    partyPreferredPods: List<String>
) {
    Draw4PingBarFromMultipleSource(
        modifier = modifier,
        pingsMs = if (partyPreferredPods.isNotEmpty()) {
            remember(memberPods, partyPreferredPods) {
                partyPreferredPods.mapNotNull { pref ->
                    memberPods.find { it.id == pref }?.ping
                }
            }
        } else {
            remember(memberPods) {
                memberPods.map { it.ping }
            }
        }
    )
}

@Composable
private fun Draw4PingBarFromMultipleSource(
    modifier: Modifier,
    pingsMs: List<Int>,
    isPingAlreadySorted: Boolean = false
) {
    val strengths = remember(pingsMs) {
        val list = mutableListOf<Int>()
        val size = pingsMs.size
        when {
            size < 1 -> {
                list.apply { repeat(4) { add(-1) } }
            }
            size == 1 -> {
                val get = pingsMs[0]
                list.apply { repeat(4) { add(pingStrengthInRangeOf4(get)) } }
            }
            else -> {
                val sort = if (isPingAlreadySorted) pingsMs else pingsMs.sorted()
                repeat(4) { i ->
                    list.add(
                        pingStrengthInRangeOf4(
                            sort.getOrElse(3 - i) {
                                return@repeat list.add(0, pingStrengthInRangeOf4(sort.first()))
                            }
                        )
                    )
                }
                list.sort()
            }
        }
        list
    }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(4) { i ->
                val s = strengths[i]
                val n = i + 1
                val height = (maxHeight.value * (n.toFloat() / 4)).dp
                val width = maxHeight / 4
                val color = when (s) {
                    -1 -> null
                    1 -> Color.Red
                    2 -> Color.Yellow
                    3, 4 -> Color.Green
                    else -> error("Unguarded condition $s")
                }
                Box(
                    modifier = Modifier
                        .height(height)
                        .width(width)
                        .background(color ?: Color.Gray)
                        .shadow(2.dp)
                )
            }
        }
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
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
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