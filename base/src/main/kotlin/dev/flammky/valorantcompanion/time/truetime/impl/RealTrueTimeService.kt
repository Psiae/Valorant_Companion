package dev.flammky.valorantcompanion.time.truetime.impl

import dev.flammky.valorantcompanion.time.truetime.LazyTimeKeeper
import dev.flammky.valorantcompanion.time.truetime.TrueTimeService
import kotlin.time.DurationUnit

internal class RealTrueTimeService : TrueTimeService {

    private val HourlyLazyTimeKeeper = HourlyLazyTimeKeeper()

    override fun getLazyTimeKeeper(resolution: DurationUnit): LazyTimeKeeper? {
        if (resolution == DurationUnit.HOURS) {
            return HourlyLazyTimeKeeper
        }
        return null
    }
}