package dev.flammky.valorantcompanion.pvp.pregame

import androidx.annotation.IntRange
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank

data class PreGamePlayerMMRData(
    val subject: String,
    val version: String,
    val competitiveRank: CompetitiveRank,
    @IntRange(from = 0, to = 100) val competitiveRankRating: Int
) {

    init {
        check(competitiveRankRating in 0..100) {
            "Invalid CompetitiveRankRating data"
        }
    }
}
