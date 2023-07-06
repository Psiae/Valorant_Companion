package dev.flammky.valorantcompanion.pvp.mmr

import androidx.annotation.IntRange
import dev.flammky.valorantcompanion.pvp.season.ValorantActiveSeason
import dev.flammky.valorantcompanion.pvp.season.ValorantSeason
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank

data class SeasonalMMRData(
    val subject: String,
    val season: ValorantActiveSeason,
    val competitiveTier: Int,
    val competitiveRank: CompetitiveRank,
    @IntRange(from = 0) val rankRating: Int
)
