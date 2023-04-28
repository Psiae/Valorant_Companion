package dev.flammky.valorantcompanion.pvp.party

import kotlinx.coroutines.Deferred

interface PartyServiceClient {

    fun fetchSignedInPlayerPartyDataAsync(puuid: String): Deferred<PlayerPartyData>
    fun changePartyMatchmakingGameMode(
        puuid: String,
        gameModeId: String
    ): Deferred<PlayerPartyData>

    fun dispose()
}