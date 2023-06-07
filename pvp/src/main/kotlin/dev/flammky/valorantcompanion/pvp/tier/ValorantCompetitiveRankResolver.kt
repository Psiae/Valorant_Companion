package dev.flammky.valorantcompanion.pvp.tier

import dev.flammky.valorantcompanion.pvp.season.ValorantSeasons
import dev.flammky.valorantcompanion.pvp.tier.internal.RealValorantCompetitiveRankResolver

interface ValorantCompetitiveRankResolver {

    fun getByTier(tier: Int): CompetitiveRank?

    companion object {

        fun getResolverOfSeason(
            episode: Int,
            act: Int
        ): ValorantCompetitiveRankResolver {
            return RealValorantCompetitiveRankResolver(
                isImmortalMerged = (episode == 2) || (episode == 3 && act < 2),
                hasAscendant = episode > 5
            )
        }

        fun getResolverOfCurrentStagedSeason(): ValorantCompetitiveRankResolver {
            return getResolverOfSeason(
                episode = ValorantSeasons.ACTIVE_STAGED.episode.num,
                act = ValorantSeasons.ACTIVE_STAGED.act.num
            )
        }
    }
}