package dev.flammky.valorantcompanion.base.time

import dev.flammky.valorantcompanion.base.time.truetime.TimeKeeperService
import dev.flammky.valorantcompanion.base.time.truetime.impl.RealTimeKeeperService
import org.koin.dsl.module as koinModule

val KoinBaseTimeModule = koinModule {
    single<TimeKeeperService> { RealTimeKeeperService() }
}