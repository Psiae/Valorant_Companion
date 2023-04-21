package dev.flammky.valorantcompanion.auth.di

import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepositoryImpl
import dev.flammky.valorantcompanion.auth.riot.internal.RiotAuthServiceImpl
import org.koin.dsl.module

val KoinAuthModule = module {
    single<RiotAuthRepository> {
        RiotAuthRepositoryImpl()
    }
    single<RiotAuthService> {
        RiotAuthServiceImpl(
            repository = get<RiotAuthRepository>() as RiotAuthRepositoryImpl
        )
    }

}