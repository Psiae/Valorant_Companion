package dev.flammky.valorantcompanion.pvp.party

import kotlinx.coroutines.Deferred

interface PartyServiceClient {

    fun fetchSignedInPlayerPartyMembersAsync(puuid: String): Deferred<List<PlayerPartyMember>>

    fun dispose()
}