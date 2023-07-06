package dev.flammky.valorantcompanion.pvp.date

import kotlinx.datetime.Instant

class ISO8601 private constructor(
    private val instant: Instant,
) {

    val epochMillisecond by lazy { instant.toEpochMilliseconds() }
    val isoString by lazy { instant.toString() }

    companion object {
        @kotlin.jvm.Throws(IllegalArgumentException::class)
        fun fromISOString(string: String): ISO8601 {
            val instant = Instant.parse(string)
            return ISO8601(instant)
        }

        fun fromEpochMilli(epochMilli: Long): ISO8601 {
            val instant = Instant.fromEpochMilliseconds(epochMilli)
            return ISO8601(instant)
        }

        fun ISO8601.isStartOfTime() = epochMillisecond == START_OF_TIME_EPOCH_MILLIS

        const val START_OF_TIME_EPOCH_MILLIS = -62135596800000
        const val START_OF_TIME_EPOCH_SECONDS = START_OF_TIME_EPOCH_MILLIS / 1000
    }

    override fun equals(other: Any?): Boolean {
        return other is ISO8601 && other.instant == this.instant
    }

    override fun hashCode(): Int {
        return instant.hashCode()
    }
}