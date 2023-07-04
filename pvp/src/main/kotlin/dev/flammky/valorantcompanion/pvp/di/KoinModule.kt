package dev.flammky.valorantcompanion.pvp.di

import dev.flammky.valorantcompanion.pvp.http.ktor.KtorWrappedHttpClient
import dev.flammky.valorantcompanion.pvp.ingame.InGameService
import dev.flammky.valorantcompanion.pvp.ingame.internal.RealInGameService
import dev.flammky.valorantcompanion.pvp.internal.AuthProviderImpl
import dev.flammky.valorantcompanion.pvp.internal.loadout.GeoProviderImpl
import dev.flammky.valorantcompanion.pvp.internal.loadout.PlayerLoadoutRepositoryImpl
import dev.flammky.valorantcompanion.pvp.internal.loadout.PlayerLoadoutServiceImpl
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadoutRepository
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadoutService
import dev.flammky.valorantcompanion.pvp.mmr.RealValorantMMRService
import dev.flammky.valorantcompanion.pvp.mmr.ValorantMMRService
import dev.flammky.valorantcompanion.pvp.party.PartyService
import dev.flammky.valorantcompanion.pvp.party.internal.RealPartyService
import dev.flammky.valorantcompanion.pvp.player.ValorantNameService
import dev.flammky.valorantcompanion.pvp.player.internal.RealValorantNameService
import dev.flammky.valorantcompanion.pvp.pregame.PreGameService
import dev.flammky.valorantcompanion.pvp.pregame.internal.RealPreGameService
import org.koin.dsl.module

val KoinPvpModule = module {
    single<PlayerLoadoutRepository> {
        PlayerLoadoutRepositoryImpl()
    }
    single<PlayerLoadoutService> {
        PlayerLoadoutServiceImpl(
            repo = get<PlayerLoadoutRepository>() as PlayerLoadoutRepositoryImpl,
            authProvider = AuthProviderImpl(get()),
            geoProvider = GeoProviderImpl(get()),
            httpClientFactory = { KtorWrappedHttpClient() }
        )
    }
    single<PartyService> {
        RealPartyService(
            authService = get(),
            geoRepository = get(),
            httpClientFactory = { KtorWrappedHttpClient() }
        )
    }
    single<ValorantNameService> {
        RealValorantNameService(KtorWrappedHttpClient(), get(), get())
    }
    single<PreGameService> {
        RealPreGameService(
            authService = get(),
            geoRepository = get(),
            httpClientFactory = { KtorWrappedHttpClient() }
        )
    }
    single<InGameService> {
        RealInGameService(
            authService = get(),
            geoRepository = get(),
            httpClientFactory = { KtorWrappedHttpClient() }
        )
    }
    single<ValorantMMRService> {
        RealValorantMMRService(
            authService = get(),
            geoRepository = get(),
            httpClientFactory = { KtorWrappedHttpClient() }
        )
    }
}