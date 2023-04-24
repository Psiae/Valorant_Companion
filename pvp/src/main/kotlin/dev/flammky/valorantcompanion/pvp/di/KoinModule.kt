package dev.flammky.valorantcompanion.pvp.di

import dev.flammky.valorantcompanion.pvp.http.ktor.KtorWrappedHttpClient
import dev.flammky.valorantcompanion.pvp.internal.AuthProviderImpl
import dev.flammky.valorantcompanion.pvp.internal.loadout.GeoProviderImpl
import dev.flammky.valorantcompanion.pvp.internal.loadout.PlayerLoadoutRepositoryImpl
import dev.flammky.valorantcompanion.pvp.internal.loadout.PlayerLoadoutServiceImpl
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadoutRepository
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadoutService
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
}