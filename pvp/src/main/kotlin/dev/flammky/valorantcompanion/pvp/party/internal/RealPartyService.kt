package dev.flammky.valorantcompanion.pvp.party.internal

import dev.flammky.valorantcompanion.pvp.http.ktor.KtorWrappedHttpClient
import dev.flammky.valorantcompanion.pvp.party.PartyService
import dev.flammky.valorantcompanion.pvp.party.PartyServiceClient

class RealPartyService : PartyService {

    override fun createClient(): PartyServiceClient {

        return DisposablePartyServiceClient(
            KtorWrappedHttpClient()
        )
    }
}