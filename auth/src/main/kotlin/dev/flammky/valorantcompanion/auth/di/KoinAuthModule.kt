package dev.flammky.valorantcompanion.auth.di

import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.internal.RiotAuthRepositoryImpl
import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.auth.riot.internal.RiotAuthServiceImpl
import dev.flammky.valorantcompanion.auth.riot.internal.RiotGeoRepositoryImpl
import org.koin.dsl.module

val KoinAuthModule = module {
    single<RiotAuthRepository> {
        RiotAuthRepositoryImpl()
    }
    single<RiotGeoRepository> {
        RiotGeoRepositoryImpl()
    }
    single<RiotAuthService> {
        RiotAuthServiceImpl(
            authRepo = get<RiotAuthRepository>() as RiotAuthRepositoryImpl,
            geo = get<RiotGeoRepository>() as RiotGeoRepositoryImpl
        )
    }

}