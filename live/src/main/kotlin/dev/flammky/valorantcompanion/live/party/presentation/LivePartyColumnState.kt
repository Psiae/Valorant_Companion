package dev.flammky.valorantcompanion.live.party.presentation

import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.pvp.party.PartyChangePreferredPodsRequest
import dev.flammky.valorantcompanion.pvp.party.PartyService
import dev.flammky.valorantcompanion.pvp.player.GetPlayerNameRequest
import dev.flammky.valorantcompanion.pvp.player.NameService
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.*
import org.koin.androidx.compose.get as getFromKoin

@Composable
fun rememberLivePartyColumnPresenter(
    nameService: NameService = getFromKoin(),
    geo: RiotGeoRepository = getFromKoin(),
    partyService: PartyService = getFromKoin()
): LivePartyColumnPresenter {
    return remember(nameService, geo) { LivePartyColumnPresenter(nameService, geo, partyService) }
}

class LivePartyColumnPresenter(
    private val nameService: NameService,
    private val geo: RiotGeoRepository,
    private val partyService: PartyService
) {

    @Composable
    fun present(
        userPuuid: String,
        partyDataState: State<PlayerPartyData?>
    ): LivePartyColumnState {
        val coroutineScope = rememberCoroutineScope()
        val partyClient = remember(this) {
            partyService.createClient()
        }
        return remember(this, userPuuid, partyDataState) {
            LivePartyColumnState(
                lookupPartyMembersName = { data ->
                    lookupPartyMembersName(coroutineScope, userPuuid, data)
                },
                partyDataState = partyDataState,
                coroutineScope = coroutineScope,
                changePreferredPods = { data, ids ->
                    partyClient.changePartyMatchmakingPreferredPods(
                        PartyChangePreferredPodsRequest(
                            userPuuid,
                            data.partyID,
                            ids,
                            build = {}
                        )
                    )
                }
            )
        }
    }

    private fun lookupPartyMembersName(
        coroutineScope: CoroutineScope,
        userPuuid: String,
        partyData: PlayerPartyData
    ): Deferred<ImmutableMap<String, Result<PlayerPartyMemberName>>> {
        val def = CompletableDeferred<ImmutableMap<String, Result<PlayerPartyMemberName>>>()

        coroutineScope.launch {

            def.completeWith(
                runCatching {
                    val geo = geo.getGeoShardInfo(userPuuid)
                        ?: error("Unable to retrieve GeoShard info")
                    val get = nameService.getPlayerNameAsync(
                        GetPlayerNameRequest(geo.shard, partyData.members.map { it.puuid })
                    ).await()
                    val map = persistentMapOf<String, Result<PlayerPartyMemberName>>().mutate { map ->
                        partyData.members.forEach { member ->
                            map[member.puuid] = run {
                                get[member.puuid]
                                    ?.let { f ->
                                        val d = f.getOrElse { t ->
                                            return@run Result.failure(t)
                                        }
                                        Result.success(
                                            PlayerPartyMemberName(
                                                d.subject,
                                                d.gameName,
                                                d.tagLine
                                            )
                                        )
                                    }
                                    ?: Result.failure(IllegalStateException("Request not found"))
                            }
                        }
                    }
                    map
                }
            )

        }.invokeOnCompletion { ex ->
            ex?.let { def.completeExceptionally(ex) }
            check(def.isCompleted)
        }

        return def
    }
}

class LivePartyColumnState(
    private val lookupPartyMembersName: (data: PlayerPartyData) -> Deferred<ImmutableMap<String, Result<PlayerPartyMemberName>>>,
    private val coroutineScope: CoroutineScope,
    val partyDataState: State<PlayerPartyData?>,
    val changePreferredPods: (data: PlayerPartyData, ids: Set<String>) -> Unit
) {
    val partyMemberNameLookupResults = mutableStateOf<ImmutableMap<String, Result<PlayerPartyMemberName>>>(
        persistentMapOf()
    )

    private var latestLookup: Job? = null

    fun lookupName() {
        latestLookup?.cancel()
        latestLookup = coroutineScope.launch {
            runCatching {
                partyDataState.value
                    ?.let { data ->
                        val fetch = lookupPartyMembersName(data).await()
                        partyMemberNameLookupResults.value = fetch
                    }
                    ?: run {
                        partyMemberNameLookupResults.value = persistentMapOf()
                    }
            }.onFailure {
                partyMemberNameLookupResults.value = persistentMapOf()
            }
        }
    }
}