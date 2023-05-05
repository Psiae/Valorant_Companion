package dev.flammky.valorantcompanion.time

import dev.flammky.valorantcompanion.time.truetime.TrueTimeService
import dev.flammky.valorantcompanion.time.truetime.impl.RealTrueTimeService
import org.koin.dsl.module as koinModule

val KoinBaseTimeModule = koinModule {
    single<TrueTimeService> { RealTrueTimeService() }
}