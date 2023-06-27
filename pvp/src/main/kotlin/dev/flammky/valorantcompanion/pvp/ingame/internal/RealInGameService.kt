package dev.flammky.valorantcompanion.pvp.ingame.internal

import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.ingame.InGameService
import dev.flammky.valorantcompanion.pvp.ingame.InGameUserClient

internal class RealInGameService(
    private val authService: RiotAuthService,
    private val geoRepository: RiotGeoRepository,
    private val httpClientFactory: () -> HttpClient
) : InGameService {

    override fun createUserClient(puuid: String): InGameUserClient {
        return RealInGameUserClient(puuid, authService, geoRepository, httpClientFactory)
    }
}