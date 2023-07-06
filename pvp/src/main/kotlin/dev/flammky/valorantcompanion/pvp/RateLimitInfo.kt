package dev.flammky.valorantcompanion.pvp

import dev.flammky.valorantcompanion.pvp.date.ISO8601
import kotlin.time.Duration

data class RateLimitInfo(
    val remoteServerStamp: ISO8601?,
    val deviceClockStamp: Long?,
    val retryAfter: Duration?
) {
}