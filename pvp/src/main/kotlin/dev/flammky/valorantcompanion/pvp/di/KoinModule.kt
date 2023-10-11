package dev.flammky.valorantcompanion.pvp.di

import dev.flammky.valorantcompanion.base.kt.sync
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.http.JsonHttpResponse
import dev.flammky.valorantcompanion.pvp.http.ktor.KtorWrappedHttpClient
import dev.flammky.valorantcompanion.pvp.ingame.InGameService
import dev.flammky.valorantcompanion.pvp.ingame.internal.RealInGameService
import dev.flammky.valorantcompanion.pvp.internal.AuthProviderImpl
import dev.flammky.valorantcompanion.pvp.internal.GeoProviderImpl
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadoutRepository
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadoutService
import dev.flammky.valorantcompanion.pvp.loadout.internal.PlayerLoadoutRepositoryImpl
import dev.flammky.valorantcompanion.pvp.loadout.internal.PlayerLoadoutServiceImpl
import dev.flammky.valorantcompanion.pvp.mmr.RealValorantMMRService
import dev.flammky.valorantcompanion.pvp.mmr.ValorantMMRService
import dev.flammky.valorantcompanion.pvp.party.PartyService
import dev.flammky.valorantcompanion.pvp.party.internal.RealPartyService
import dev.flammky.valorantcompanion.pvp.player.ValorantNameService
import dev.flammky.valorantcompanion.pvp.player.internal.RealValorantNameService
import dev.flammky.valorantcompanion.pvp.pregame.PreGameService
import dev.flammky.valorantcompanion.pvp.pregame.internal.RealPreGameService
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreService
import dev.flammky.valorantcompanion.pvp.store.internal.RiotValorantStoreEndpoint
import dev.flammky.valorantcompanion.pvp.store.internal.RiotValorantStoreResponseHandler
import dev.flammky.valorantcompanion.pvp.store.internal.ValorantStoreServiceImpl
import kotlinx.atomicfu.atomic
import org.koin.dsl.module
import kotlin.coroutines.cancellation.CancellationException

val KoinPvpModule = module {
    // TODO: I think we should just Inject HttpClient from another koin module
    var httpClient: HttpClient? = null
    var keepAliveCount = 0
    val httpClientFactory = {
        sync {
            if (keepAliveCount++ == 0) {
                httpClient = KtorWrappedHttpClient()
            }
            val client = httpClient!!
            val disposed = atomic(false)
            object : HttpClient() {

                override suspend fun jsonRequest(request: JsonHttpRequest): JsonHttpResponse {
                    if (disposed.value) throw CancellationException("Already Disposed")
                    return client.jsonRequest(request)
                }

                override fun dispose() {
                    sync {
                        if (--keepAliveCount == 0) {
                            client.dispose()
                            httpClient = null
                        }
                    }
                    disposed.value = true
                }
            }
        }
    }
    single<PlayerLoadoutRepository> {
        PlayerLoadoutRepositoryImpl()
    }
    single<PlayerLoadoutService> {
        PlayerLoadoutServiceImpl(
            repo = get<PlayerLoadoutRepository>() as PlayerLoadoutRepositoryImpl,
            authProvider = AuthProviderImpl(get()),
            geoProvider = GeoProviderImpl(get()),
            httpClientFactory = httpClientFactory
        )
    }
    single<PartyService> {
        RealPartyService(
            authService = get(),
            geoRepository = get(),
            httpClientFactory = httpClientFactory
        )
    }
    single<ValorantNameService> {
        RealValorantNameService(httpClientFactory, get(), get())
    }
    single<PreGameService> {
        RealPreGameService(
            authService = get(),
            geoRepository = get(),
            httpClientFactory = httpClientFactory
        )
    }
    single<InGameService> {
        RealInGameService(
            authService = get(),
            geoRepository = get(),
            httpClientFactory = httpClientFactory
        )
    }
    single<ValorantMMRService> {
        RealValorantMMRService(
            authService = get(),
            geoRepository = get(),
            httpClientFactory = httpClientFactory
        )
    }
    single<ValorantStoreService> {
        ValorantStoreServiceImpl(
            auth = AuthProviderImpl(get()),
            geo = GeoProviderImpl(get()),
            endpoint = RiotValorantStoreEndpoint(),
            responseHandler = RiotValorantStoreResponseHandler(),
            httpClientFactory = httpClientFactory
        )
    }
}