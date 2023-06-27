package dev.flammky.valorantcompanion.pvp.tier.internal

import dev.flammky.valorantcompanion.pvp.ex.RankNotPresentInSeasonException
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import dev.flammky.valorantcompanion.pvp.tier.ValorantCompetitiveRankResolver

class RealValorantCompetitiveRankResolver(
    val episode: Int,
    val act: Int,
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
            27 -> CompetitiveRank.RADIANT
            else -> null
        }
    }

    override fun localizeTier(competitiveRank: CompetitiveRank): Int {
        return when(competitiveRank) {
            CompetitiveRank.ASCENDANT_1 -> {
                expectPresent(hasAscendant) {
                    "Ascendant 1 is not present in the given season"
                }
                21
            }
            CompetitiveRank.ASCENDANT_2 -> {
                expectPresent(hasAscendant) {
                    "Ascendant 2 is not present in the given season"
                }
                22
            }
            CompetitiveRank.ASCENDANT_3 -> {
                expectPresent(hasAscendant) {
                    "Ascendant 3 is not present in the given season"
                }
                23
            }
            CompetitiveRank.BRONZE_1 -> 6
            CompetitiveRank.BRONZE_2 -> 7
            CompetitiveRank.BRONZE_3 -> 8
            CompetitiveRank.DIAMOND_1 -> 18
            CompetitiveRank.DIAMOND_2 -> 19
            CompetitiveRank.DIAMOND_3 -> 20
            CompetitiveRank.GOLD_1 -> 12
            CompetitiveRank.GOLD_2 -> 13
            CompetitiveRank.GOLD_3 -> 14
            CompetitiveRank.IMMORTAL_1 -> {
                expectPresent(!isImmortalMerged) {
                    "Immortal was merged in the given season ($episode, $act)"
                }
                if (hasAscendant) 24 else 21
            }
            CompetitiveRank.IMMORTAL_2 -> {
                expectPresent(!isImmortalMerged) {
                    "Immortal was merged in the given season ($episode, $act)"
                }
                if (hasAscendant) 25 else 22
            }
            CompetitiveRank.IMMORTAL_3 -> {
                expectPresent(!isImmortalMerged) {
                    "Immortal was merged in the given season ($episode, $act)"
                }
                if (hasAscendant) 26 else 23
            }
            CompetitiveRank.IMMORTAL_MERGED -> {
                expectPresent(isImmortalMerged) {
                    "Immortal was not merged in the given season ($episode, $act)"
                }
                21
            }
            CompetitiveRank.IRON_1 -> 3
            CompetitiveRank.IRON_2 -> 4
            CompetitiveRank.IRON_3 -> 5
            CompetitiveRank.PLATINUM_1 -> 15
            CompetitiveRank.PLATINUM_2 -> 16
            CompetitiveRank.PLATINUM_3 -> 17
            CompetitiveRank.RADIANT -> {
                // when immortal was merged, radiant stays at 24
                if (hasAscendant) 27 else 24
            }
            CompetitiveRank.SILVER_1 -> 9
            CompetitiveRank.SILVER_2 -> 10
            CompetitiveRank.SILVER_3 -> 11
            CompetitiveRank.UNRANKED -> 0
            CompetitiveRank.UNUSED_1 -> 1
            CompetitiveRank.UNUSED_2 -> 2
        }
    }

    override fun isRankPresent(competitiveRank: CompetitiveRank): Boolean {
        return when(competitiveRank) {
            CompetitiveRank.ASCENDANT_1, CompetitiveRank.ASCENDANT_2, CompetitiveRank.ASCENDANT_3 -> hasAscendant
            CompetitiveRank.IMMORTAL_1, CompetitiveRank.IMMORTAL_2, CompetitiveRank.IMMORTAL_3 -> !isImmortalMerged
            CompetitiveRank.IMMORTAL_MERGED -> isImmortalMerged
            else -> true
        }
    }

    private fun expectPresent(present: Boolean, message: () -> Any)  {
        if (!present) throw RankNotPresentInSeasonException(message().toString())
    }
}