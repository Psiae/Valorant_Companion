package dev.flammky.valorantcompanion.pvp.party

import dev.flammky.valorantcompanion.base.time.ISO8601
import kotlinx.collections.immutable.ImmutableList

data class QueueIneligibilityData(
    val subject: String,
    val queueIds: ImmutableList<String>,
    val reason: String,
    val expiry: ISO8601?
) {
}