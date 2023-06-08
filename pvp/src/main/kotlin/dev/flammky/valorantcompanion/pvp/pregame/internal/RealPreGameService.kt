package dev.flammky.valorantcompanion.pvp.pregame.internal

import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.pvp.http.ktor.KtorWrappedHttpClient
import dev.flammky.valorantcompanion.pvp.pregame.PreGameUserClient
import dev.flammky.valorantcompanion.pvp.pregame.PreGameService
import kotlinx.coroutines.SupervisorJob

class RealPreGameService(
    private val authService: RiotAuthService,
    private val geoRepository: RiotGeoRepository
) : PreGameService {

    private val lifetime = SupervisorJob()

    override fun createUserClient(puuid: String): PreGameUserClient {
        return DisposablePreGameUserClient(
            puuid = puuid,
            httpClient = KtorWrappedHttpClient(lifetime = lifetime),
            auth = authService,
            geo = geoRepository
        )
    }
}