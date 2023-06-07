package dev.flammky.valorantcompanion.pvp.tier.internal

import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import dev.flammky.valorantcompanion.pvp.tier.ValorantCompetitiveRankResolver

class RealValorantCompetitiveRankResolver(
    val isImmortalMerged: Boolean,
    val hasAscendant: Boolean
) : ValorantCompetitiveRankResolver {

    override fun getByTier(tier: Int): CompetitiveRank? {
        return when(tier) {
            0 -> CompetitiveRank.UNRANKED
            1 -> CompetitiveRank.UNUSED_1
            2 -> CompetitiveRank.UNUSED_2
            3 -> CompetitiveRank.IRON_1
            4 -> CompetitiveRank.IRON_2
            5 -> CompetitiveRank.IRON_3
            6 -> CompetitiveRank.BRONZE_1
            7 -> CompetitiveRank.BRONZE_2
            8 -> CompetitiveRank.BRONZE_3
            9 -> CompetitiveRank.SILVER_1
            10 -> CompetitiveRank.SILVER_2
            11 -> CompetitiveRank.SILVER_3
            12 -> CompetitiveRank.GOLD_1
            13 -> CompetitiveRank.GOLD_2
            14 -> CompetitiveRank.GOLD_3
            15 -> CompetitiveRank.PLATINUM_1
            16 -> CompetitiveRank.PLATINUM_2
            17 -> CompetitiveRank.PLATINUM_3
            18 -> CompetitiveRank.DIAMOND_1
            19 -> CompetitiveRank.DIAMOND_2
            20 -> CompetitiveRank.DIAMOND_3
            21 -> if (hasAscendant) {
                CompetitiveRank.ASCENDANT_1
            } else {
                if (isImmortalMerged) CompetitiveRank.IMMORTAL_MERGED else CompetitiveRank.IMMORTAL_1
            }
            22 -> if (hasAscendant) {
                CompetitiveRank.ASCENDANT_2
            } else {
                if (isImmortalMerged) CompetitiveRank.IMMORTAL_MERGED else CompetitiveRank.IMMORTAL_2
            }
            23 -> if (hasAscendant) {
                CompetitiveRank.ASCENDANT_3
            } else {
                if (isImmortalMerged) CompetitiveRank.IMMORTAL_MERGED else CompetitiveRank.IMMORTAL_3
            }
            24 -> if (hasAscendant) {
                CompetitiveRank.IMMORTAL_1
            } else {
                CompetitiveRank.RADIANT
            }
            25 -> if (hasAscendant) {
                CompetitiveRank.IMMORTAL_2
            } else {
                CompetitiveRank.RADIANT
            }
            26 -> if (hasAscendant) {
                CompetitiveRank.IMMORTAL_3
            } else {
                CompetitiveRank.RADIANT
            }
            27 -> if (hasAscendant) {
                CompetitiveRank.IMMORTAL_3
            } else {
                CompetitiveRank.RADIANT
            }
            else -> null
        }
    }
}