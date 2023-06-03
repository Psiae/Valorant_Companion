package dev.flammky.valorantcompanion.pvp.di

import dev.flammky.valorantcompanion.pvp.http.ktor.KtorWrappedHttpClient
import dev.flammky.valorantcompanion.pvp.internal.AuthProviderImpl
import dev.flammky.valorantcompanion.pvp.internal.loadout.GeoProviderImpl
import dev.flammky.valorantcompanion.pvp.internal.loadout.PlayerLoadoutRepositoryImpl
import dev.flammky.valorantcompanion.pvp.internal.loadout.PlayerLoadoutServiceImpl
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadoutRepository
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadoutService
import dev.flammky.valorantcompanion.pvp.party.PartyService
import dev.flammky.valorantcompanion.pvp.party.internal.RealPartyService
import dev.flammky.valorantcompanion.pvp.player.NameService
import dev.flammky.valorantcompanion.pvp.player.internal.RealNameService
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
            httpClient = KtorWrappedHttpClient()
        )
    }
    single<PartyService> {
        RealPartyService(
            authService = get(),
            geoRepository = get()
        )
    }
    single<NameService> {
        RealNameService(KtorWrappedHttpClient(), get())
    }
    single<PreGameService> {
        RealPreGameService(
            authService = get(),
            geoRepository = get()
        )
    }
}