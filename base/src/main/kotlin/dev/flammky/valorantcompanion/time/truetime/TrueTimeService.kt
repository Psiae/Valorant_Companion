package dev.flammky.valorantcompanion.time.truetime

import kotlin.time.DurationUnit

interface TrueTimeService {

    fun getLazyTimeKeeper(
        resolution: DurationUnit
    ): LazyTimeKeeper?
}