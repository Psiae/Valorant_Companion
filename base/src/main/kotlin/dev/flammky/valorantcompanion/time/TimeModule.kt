package dev.flammky.valorantcompanion.time

import dev.flammky.valorantcompanion.time.truetime.TimeKeeperService
import dev.flammky.valorantcompanion.time.truetime.impl.RealTimeKeeperService
import org.koin.dsl.module as koinModule

val KoinBaseTimeModule = koinModule {
    single<TimeKeeperService> { RealTimeKeeperService() }
}