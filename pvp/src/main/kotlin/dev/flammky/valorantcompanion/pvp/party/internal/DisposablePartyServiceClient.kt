package dev.flammky.valorantcompanion.pvp.party.internal

import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.party.PartyServiceClient

internal class DisposablePartyServiceClient(
    private val httpClient: HttpClient
) : PartyServiceClient {



    override fun dispose() {

    }
}