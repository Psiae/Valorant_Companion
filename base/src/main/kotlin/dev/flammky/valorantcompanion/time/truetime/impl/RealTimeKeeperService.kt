package dev.flammky.valorantcompanion.time.truetime.impl

import dev.flammky.valorantcompanion.time.truetime.LazyTimeKeeper
import dev.flammky.valorantcompanion.time.truetime.TimeKeeperService
import kotlin.time.DurationUnit

internal class RealTimeKeeperService : TimeKeeperService {

    private val HourlyLazyTimeKeeper = HourlyLazyTimeKeeper()

    override fun getLazyTimeKeeper(resolution: DurationUnit): LazyTimeKeeper? {
        if (resolution == DurationUnit.HOURS) {
            return HourlyLazyTimeKeeper
        }
        return null
    }

    override fun getLazyTimeKeeperOfAtLeast(resolution: DurationUnit): LazyTimeKeeper? {
        return if (resolution < DurationUnit.HOURS) null else HourlyLazyTimeKeeper
    }

    override fun getLazyTimeKeeperOfAtMost(resolution: DurationUnit): LazyTimeKeeper {
        return HourlyLazyTimeKeeper
    }

    override fun getLazyTimeKeeper(): LazyTimeKeeper {
        return HourlyLazyTimeKeeper
    }
}