package dev.flammky.valorantcompanion.time.truetime

import kotlin.time.DurationUnit

interface TimeKeeperService {

    fun getLazyTimeKeeper(
        resolution: DurationUnit
    ): LazyTimeKeeper?

    fun getLazyTimeKeeperOfAtLeast(resolution: DurationUnit): LazyTimeKeeper?

    fun getLazyTimeKeeperOfAtMost(resolution: DurationUnit): LazyTimeKeeper

    fun getLazyTimeKeeper(): LazyTimeKeeper
}