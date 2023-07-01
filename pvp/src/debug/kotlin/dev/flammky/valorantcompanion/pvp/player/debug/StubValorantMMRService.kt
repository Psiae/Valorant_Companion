package dev.flammky.valorantcompanion.pvp.player.debug

import dev.flammky.valorantcompanion.pvp.ex.SeasonalMMRDataNotFoundException
import dev.flammky.valorantcompanion.pvp.mmr.MMRUserClient
import dev.flammky.valorantcompanion.pvp.mmr.SeasonalMMRData
import dev.flammky.valorantcompanion.pvp.mmr.ValorantMMRService
import dev.flammky.valorantcompanion.pvp.season.ValorantSeasons
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import dev.flammky.valorantcompanion.pvp.tier.ValorantCompetitiveRankResolver
import kotlinx.coroutines.*

typealias StubSeasonalMMRProvider = (season: String, subject: String) -> SeasonalMMRData?

class StubValorantMMRService(
    private val provider: StubSeasonalMMRProvider
) : ValorantMMRService {

    override fun createUserClient(puuid: String): MMRUserClient {
        return StubValorantMMRUserClient()
    }

    private inner class StubValorantMMRUserClient: MMRUserClient {

        private val coroutineScope = CoroutineScope(SupervisorJob())

        override fun fetchSeasonalMMR(
            season: String,
            subject: String
        ): Deferred<Result<SeasonalMMRData>> {
            val def = CompletableDeferred<Result<SeasonalMMRData>>()

            val task = coroutineScope.launch(Dispatchers.IO) {
                def.complete(
                    provider(season, subject)
                        ?.let { Result.success(it) }
                        ?: Result.failure(SeasonalMMRDataNotFoundException())
                )
            }

            def.invokeOnCompletion { task.cancel() }

            return def
        }
    }


    companion object {
        val DEFAULT_FAKE_PROVIDER: StubSeasonalMMRProvider = provider@ { season, subject ->
            val resolveSeason = ValorantSeasons.ofId(season)
                ?: return@provider null
            val rankResolver = ValorantCompetitiveRankResolver.getResolverOfSeason(
                resolveSeason.episode.num,
                resolveSeason.act.num
            )
            when (subject) {
                "dokka" -> {
                    val ascendantPresent = rankResolver
                        .isRankPresent(CompetitiveRank.ASCENDANT_3)
                    val seasonalRank =
                        if (ascendantPresent) CompetitiveRank.ASCENDANT_3
                        else CompetitiveRank.DIAMOND_3
                    SeasonalMMRData(
                        season = resolveSeason,
                        competitiveTier = rankResolver.localizeTier(seasonalRank),
                        competitiveRank = seasonalRank,
                        rankRating = 20
                    )
                }
                "dex" -> {
                    val ascendantPresent = rankResolver
                        .isRankPresent(CompetitiveRank.ASCENDANT_2)
                    val seasonalRank =
                        if (ascendantPresent) CompetitiveRank.ASCENDANT_2
                        else CompetitiveRank.DIAMOND_3
                    SeasonalMMRData(
                        season = resolveSeason,
                        competitiveTier = rankResolver.localizeTier(seasonalRank),
                        competitiveRank = seasonalRank,
                        rankRating = 45
                    )
                }
                "moon" -> {
                    val immortalPresent = rankResolver
                        .isRankPresent(CompetitiveRank.IMMORTAL_1)
                    val seasonalRank =
                        if (immortalPresent) CompetitiveRank.IMMORTAL_1
                        else CompetitiveRank.IMMORTAL_MERGED
                    SeasonalMMRData(
                        season = resolveSeason,
                        competitiveTier = rankResolver.localizeTier(seasonalRank),
                        competitiveRank = seasonalRank,
                        rankRating = 35
                    )
                }
                "hive" -> {
                    val seasonalRank = CompetitiveRank.DIAMOND_3
                    SeasonalMMRData(
                        season = resolveSeason,
                        competitiveTier = rankResolver.localizeTier(seasonalRank),
                        competitiveRank = seasonalRank,
                        rankRating = 70
                    )
                }
                "lock" -> {
                    val immortalPresent = rankResolver
                        .isRankPresent(CompetitiveRank.IMMORTAL_2)
                    val seasonalRank =
                        if (immortalPresent) CompetitiveRank.IMMORTAL_2
                        else CompetitiveRank.IMMORTAL_MERGED
                    SeasonalMMRData(
                        season = resolveSeason,
                        competitiveTier = rankResolver.localizeTier(seasonalRank),
                        competitiveRank = seasonalRank,
                        rankRating = 99
                    )
                }
                else -> null
            }
        }
    }
}