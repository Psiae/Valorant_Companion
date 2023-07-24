package dev.flammky.valorantcompanion.pvp.date

import kotlinx.datetime.Instant
import java.util.Objects
import kotlin.time.DurationUnit

class ISO8601 private constructor(
    private val instant: Instant,
    private val resolution: DurationUnit?
) {

    val epochMillisecond by lazy { instant.toEpochMilliseconds() }
    val isoString by lazy { instant.toString() }

    companion object {
        @kotlin.jvm.Throws(IllegalArgumentException::class)
        fun fromISOString(string: String, resolution: DurationUnit? = null): ISO8601 {
            val instant = Instant.parse(string)
            return ISO8601(instant, resolution)
        }

        fun fromEpochMilli(epochMilli: Long, resolution: DurationUnit? = null): ISO8601 {
            val instant = Instant.fromEpochMilliseconds(epochMilli)
            return ISO8601(instant, resolution)
        }

        fun ISO8601.isStartOfTime() = epochMillisecond == START_OF_TIME_EPOCH_MILLIS

        const val START_OF_TIME_EPOCH_MILLIS = -62135596800000
        const val START_OF_TIME_EPOCH_SECONDS = START_OF_TIME_EPOCH_MILLIS / 1000

        fun ISO8601.hasResolution(): Boolean = resolution != null
        fun ISO8601.isResolution(durationUnit: DurationUnit) = resolution == durationUnit
    }

    override fun equals(other: Any?): Boolean {
        return other is ISO8601 && other.instant == this.instant && other.resolution == this.resolution
    }

    override fun hashCode(): Int {
        return Objects.hash(instant, resolution)
    }
}