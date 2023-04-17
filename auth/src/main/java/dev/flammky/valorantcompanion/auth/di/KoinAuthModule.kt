package dev.flammky.valorantcompanion.auth.di

import dev.flammky.valorantcompanion.auth.RiotAuthService
import dev.flammky.valorantcompanion.auth.RiotAuthServiceImpl
import org.koin.dsl.module

val KoinAuthModule = module {
    single<RiotAuthService> {
        RiotAuthServiceImpl()
    }
}