package dev.flammky.valorantcompanion.pvp.party

import kotlinx.coroutines.Deferred

interface PartyServiceClient {

    fun fetchSignedInPlayerPartyDataAsync(puuid: String): Deferred<PlayerPartyData>
    fun changePartyMatchmakingQueue(request: PartyChangeQueueRequest): Deferred<Result<PartyChangeQueueRequestResult>>
    fun changePartyMatchmakingPreferredPods(request: PartyChangePreferredPodsRequest): Deferred<Result<PartyChangePreferredPodsRequestResult>>

    fun partyJoinMatchmaking(
        puuid: String,
        partyId: String,
    )

    fun partyLeaveMatchmaking(
        puuid: String,
        partyId: String,
    )

    fun dispose()
}