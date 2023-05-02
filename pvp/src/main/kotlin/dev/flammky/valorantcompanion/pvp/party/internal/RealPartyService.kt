package dev.flammky.valorantcompanion.pvp.party.internal

import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.pvp.http.ktor.KtorWrappedHttpClient
import dev.flammky.valorantcompanion.pvp.party.PartyService
import dev.flammky.valorantcompanion.pvp.party.PartyServiceClient

internal class RealPartyService(
    private val authService: RiotAuthService,
    private val geoRepository: RiotGeoRepository
) : PartyService {

    override fun createClient(): PartyServiceClient {

        return DisposablePartyServiceClient(
            KtorWrappedHttpClient(),
            authService, geoRepository
        )
    }
}