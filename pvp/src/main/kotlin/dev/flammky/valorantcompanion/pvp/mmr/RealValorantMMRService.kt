package dev.flammky.valorantcompanion.pvp.mmr

import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.pvp.http.HttpClient

// TODO: due to endpoint nature, we should provide a supervisor on rate-limit,
//  once any of the request got the limit then we send a fail signal to all registered Job
internal class RealValorantMMRService(
    private val authService: RiotAuthService,
    private val geoRepository: RiotGeoRepository,
    private val httpClientFactory: () -> HttpClient
) : ValorantMMRService {

    override fun createUserClient(puuid: String): ValorantMMRUserClient {
        return RealValorantValorantMMRUserClient(
            puuid = puuid,
            httpClient = httpClientFactory.invoke(),
            auth = authService,
            geo = geoRepository
        )
    }
}