package dev.flammky.valorantcompanion.pvp.mmr.debug

import dev.flammky.valorantcompanion.pvp.PVPAsyncRequestResult
import dev.flammky.valorantcompanion.pvp.ex.SeasonalMMRDataNotFoundException
import dev.flammky.valorantcompanion.pvp.mmr.*
import dev.flammky.valorantcompanion.pvp.season.ValorantSeasons
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import dev.flammky.valorantcompanion.pvp.tier.ValorantCompetitiveRankResolver
import kotlinx.coroutines.*

typealias StubSeasonalMMRProvider = (season: String, subject: String) -> SeasonalMMRData?

class StubValorantMMRService(
    private val provider: StubSeasonalMMRProvider
) : ValorantMMRService {



    override fun createUserClient(puuid: String): ValorantMMRUserClient {
        return StubValorantValorantMMRUserClient()
    }

    private inner class StubValorantValorantMMRUserClient: ValorantMMRUserClient {

        private val coroutineScope = CoroutineScope(SupervisorJob())

        override fun fetchSeasonalMMRAsync(
            season: String,
            subject: String
        ): Deferred<PVPAsyncRequestResult<FetchSeasonalMMRResult>> {
            val def = CompletableDeferred<PVPAsyncRequestResult<FetchSeasonalMMRResult>>()

            coroutineScope.launch(Dispatchers.IO) {
                def.complete(
                    provider(season, subject)
                        ?.let { PVPAsyncRequestResult.success(FetchSeasonalMMRResult.success(it)) }
                        ?: PVPAsyncRequestResult.failure(SeasonalMMRDataNotFoundException(), 19404)
                )
            }.apply {
                invokeOnCompletion { ex -> if (ex is CancellationException) def.cancel() }
                def.invokeOnCompletion { ex -> if (ex is CancellationException) cancel() }
            }

            return def
        }

        override fun createMatchClient(matchID: String): ValorantMMRUserMatchClient {
            TODO("Not yet implemented")
        }

        override fun dispose() {
            coroutineScope.cancel()
        }
    }


    companion object {
        val DEFAULT_FAKE_PROVIDER: StubSeasonalMMRProvider = provider@ { season, subject ->
            val resolveSeason = ValorantSeasons.ofActID(season)
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
                        subject = subject,
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
                        subject = subject,
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
                        subject = subject,
                        season = resolveSeason,
                        competitiveTier = rankResolver.localizeTier(seasonalRank),
                        competitiveRank = seasonalRank,
                        rankRating = 35
                    )
                }
                "hive" -> {
                    val seasonalRank = CompetitiveRank.DIAMOND_3
                    SeasonalMMRData(
                        subject = subject,
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
                        subject = subject,
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