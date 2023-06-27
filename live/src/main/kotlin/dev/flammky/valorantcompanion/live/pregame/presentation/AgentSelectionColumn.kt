package dev.flammky.valorantcompanion.live.pregame.presentation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.base.di.koin.getFromKoin
import dev.flammky.valorantcompanion.pvp.player.GetPlayerNameRequest
import dev.flammky.valorantcompanion.pvp.player.ValorantNameService
import dev.flammky.valorantcompanion.pvp.player.PlayerPVPName
import dev.flammky.valorantcompanion.pvp.pregame.PreGameService
import dev.flammky.valorantcompanion.pvp.pregame.onFailure
import dev.flammky.valorantcompanion.pvp.pregame.onSuccess
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgentSelectionColumn(
    modifier: Modifier,
    state: AgentSelectionState
) {
    val poll = remember(state.stateContinuationKey) {
        mutableStateOf(0L)
    }.apply {
        Log.d("AgentSelectionColumn.kt", "AgentSelectionColumn: ver=${state.stateVersion}")
        value = state.stateVersion - value
        check(value >= 0)
    }.value
    val nameResults = nameLookup(
        lookupPuuid = state.ally?.players?.map { it.puuid } ?: emptyList(),
        user = state.user?.puuid ?: "",
        poll = poll,
        pollContinuationKey = state.stateContinuationKey
    )
    val rankResults = rankLookup(
        user = state.user?.puuid ?: "",
        lookupPuuid = state.ally?.players?.map { it.puuid } ?: emptyList(),
        poll = poll,
        pollContinuationKey = state.stateContinuationKey
    )
    
    Column(modifier = modifier.heightIn(min = ((42 + 5) * 5).dp)) {
        state.ally?.players?.forEachIndexed { i, player ->
            AgentSelectionPlayerCard(
                state = rememberAgentSelectionPlayerCardPresenter()
                    .present(
                        isUser = player.puuid == state.user?.puuid,
                        player = player,
                        inUserParty = player.puuid in state.partyMembers,
                        nameData = nameResults[player.puuid],
                        rankData = rankResults[player.puuid],
                        index = i
                    )
            )
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
private fun nameLookup(
    nameService: ValorantNameService = getFromKoin(),
    geoRepository: RiotGeoRepository = getFromKoin(),
    user: String,
    lookupPuuid: List<String>,
    poll: Long,
    pollContinuationKey: Any,
): Map<String, Result<PlayerPVPName>> {
    val returns = remember(user) {
        mutableStateOf<Map<String, Result<PlayerPVPName>>>(emptyMap())
    }
    val polls = remember(pollContinuationKey) {
        mutableStateOf<Long>(0)
    }.apply {
        value += poll
    }
    val upLookupPuuid = rememberUpdatedState(newValue = lookupPuuid)
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(
        key1 = nameService,
        key2 = user,
        key3 = pollContinuationKey,
        effect = {
            val supervisor = SupervisorJob()
            var latestJob: Job? = null
            coroutineScope.launch(supervisor) {
                snapshotFlow { upLookupPuuid.value }
                    .distinctUntilChanged()
                    .collect() { list ->
                        latestJob?.cancel()
                        latestJob = coroutineScope.launch(supervisor) {
                            returns.value = emptyMap()
                            while (true) {
                                if (polls.value == 0L) snapshotFlow { polls.value }.first { it > 0 }
                                val consume = polls.value
                                Log.d("AgentSelectionColumn.kt", "nameLookup: new poll ($consume)")
                                val targets = list.filter { subject ->
                                    returns.value[subject]?.isSuccess != true
                                }
                                if (targets.isNotEmpty()) {
                                    run fetch@ {
                                        val geo = geoRepository.getGeoShardInfo(user)
                                            ?: run {
                                                returns.value = emptyMap()
                                                return@fetch
                                            }
                                        val def = nameService
                                            .getPlayerNameAsync(
                                                GetPlayerNameRequest(
                                                    shard = geo.shard,
                                                    signedInUserPUUID = user,
                                                    lookupPUUIDs = targets
                                                )
                                            )
                                        coroutineContext.job.invokeOnCompletion { def.cancel() }
                                        returns.value = def.await()
                                    }
                                } else {
                                    polls.value -= consume
                                    break
                                }
                                polls.value -= consume
                                Log.d("AgentSelectionColumn.kt", "nameLookup: end poll (${polls.value}, $consume)")
                                check(polls.value >= 0)
                            }
                        }
                    }
            }.invokeOnCompletion {
                latestJob?.cancel()
            }

            onDispose { supervisor.cancel() }
        }
    )

    return returns.value
}

@Composable
private fun rankLookup(
    preGameService: PreGameService = getFromKoin(),
    user: String,
    lookupPuuid: List<String>,
    poll: Long,
    pollContinuationKey: Any,
): Map<String, Result<CompetitiveRank>> {
    val returns = remember(user) {
        mutableStateOf<Map<String, Result<CompetitiveRank>>>(emptyMap())
    }
    val polls = remember(pollContinuationKey) {
        mutableStateOf<Long>(0)
    }.apply {
        value += poll
    }
    val preGameClient = remember(preGameService, user) {
        preGameService.createUserClient(user)
    }
    val upLookupPuuid = rememberUpdatedState(newValue = lookupPuuid)
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(
        key1 = preGameClient,
        key2 = pollContinuationKey,
        effect = {
            Log.d("AgentSelectionColumn.kt", "rankLookup LaunchedEffect ($preGameClient | $pollContinuationKey)")
            if (user.isBlank()) {
                return@DisposableEffect onDispose { preGameClient.dispose() }
            }
            val supervisor = SupervisorJob()
            var latestJob: Job? = null
            coroutineScope.launch(supervisor) {
                snapshotFlow { upLookupPuuid.value }
                    .distinctUntilChanged()
                    .collect() { list ->
                        Log.d("AgentSelectionColumn.kt", "rankLookup new upLookupPuuid")
                        latestJob?.cancel()
                        latestJob = coroutineScope.launch(supervisor) {
                            returns.value = emptyMap()
                            while (true) {
                                if (polls.value == 0L) snapshotFlow { polls.value }.first { it > 0 }
                                val consume = polls.value
                                Log.d("AgentSelectionColumn.kt", "rankLookup: new poll ($consume)")
                                val targets = list.filter { subject ->
                                    returns.value[subject]?.isSuccess != true
                                }
                                if (targets.isNotEmpty()) {
                                    run fetch@{
                                        list.forEach { subject ->
                                            if (returns.value[subject]?.isSuccess == true) {
                                                return@forEach
                                            }
                                            val def = preGameClient
                                                .fetchPlayerMMRData(subject)
                                            coroutineContext.job.invokeOnCompletion { def.cancel() }
                                            def.await()
                                                .onSuccess { data ->
                                                    returns.value =
                                                        persistentMapOf<String, Result<CompetitiveRank>>()
                                                            .builder()
                                                            .apply {
                                                                putAll(returns.value)
                                                                put(
                                                                    subject,
                                                                    Result.success(data.competitiveRank)
                                                                )
                                                            }
                                                            .build()
                                                }
                                                .onFailure { ex, _ ->
                                                    returns.value =
                                                        persistentMapOf<String, Result<CompetitiveRank>>()
                                                            .builder()
                                                            .apply {
                                                                putAll(returns.value)
                                                                put(subject, Result.failure(ex))
                                                            }
                                                            .build()
                                                }
                                        }
                                    }
                                } else {
                                    polls.value -= consume
                                    break
                                }
                                polls.value -= consume
                                Log.d("AgentSelectionColumn.kt", "rankLookup: end poll (${polls.value}, $consume)")
                                check(polls.value >= 0)
                                delay(1000)
                            }
                        }
                    }
            }.invokeOnCompletion {
                latestJob?.cancel()
            }

            onDispose { preGameClient.dispose() ; supervisor.cancel() }
        }
    )

    return returns.value
}