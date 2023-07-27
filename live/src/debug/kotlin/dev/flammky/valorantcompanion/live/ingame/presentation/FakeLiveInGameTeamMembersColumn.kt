package dev.flammky.valorantcompanion.live.ingame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.debug.DebugValorantAssetService
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.koin.compose.KoinDependencyInjector
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundColorAsState
import dev.flammky.valorantcompanion.live.pvp.ingame.presentation.LiveInGameTeamMembersColumn
import dev.flammky.valorantcompanion.live.pvp.ingame.presentation.TeamMember
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import dev.flammky.valorantcompanion.pvp.mmr.SeasonalMMRData
import dev.flammky.valorantcompanion.pvp.mmr.ValorantMMRService
import dev.flammky.valorantcompanion.pvp.player.PlayerPVPName
import dev.flammky.valorantcompanion.pvp.player.ValorantNameService
import dev.flammky.valorantcompanion.pvp.mmr.debug.StubValorantMMRService
import dev.flammky.valorantcompanion.pvp.mmr.debug.StubValorantNameService
import dev.flammky.valorantcompanion.pvp.season.ValorantSeasons
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import dev.flammky.valorantcompanion.pvp.tier.ValorantCompetitiveRankResolver
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import org.koin.core.context.GlobalContext
import org.koin.dsl.module

@Composable
@Preview
private fun FakeLiveInGameTeamMembersPreview() {

    val provisionedState = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(
        key1 = Unit,
        block = {
            GlobalContext.startKoin {
                modules(
                    module() {
                        single<ValorantAssetsService> {
                            DebugValorantAssetService()
                        }
                        single<ValorantNameService> {
                            StubValorantNameService(
                                map = persistentMapOf(
                                    "dokka" to PlayerPVPName(
                                        "dokka",
                                        "Dokka",
                                        "Dokka",
                                        "zap"
                                    ),
                                    "dex" to PlayerPVPName(
                                        "dex",
                                        "Dex",
                                        "Dex",
                                        "301"
                                    ),
                                    "moon" to PlayerPVPName(
                                        "moon",
                                        "Moon",
                                        "Moon",
                                        "301"
                                    ),
                                    "hive" to PlayerPVPName(
                                        "hive",
                                        "Hive",
                                        "Hive",
                                        "301"
                                    ),
                                    "lock" to PlayerPVPName(
                                        "lock",
                                        "Lock",
                                        "Lock",
                                        "301"
                                    )
                                )
                            )
                        }
                        single<ValorantMMRService> {
                            StubValorantMMRService(
                                provider = { season, subject ->
                                    val resolveSeason = ValorantSeasons.ofId(season)
                                        ?: return@StubValorantMMRService null
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
                                            val seasonalRank = CompetitiveRank.RADIANT
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
                            )
                        }
                    }
                )
            }
            provisionedState.value = true
        }
    )

    CompositionLocalProvider(
        LocalDependencyInjector provides KoinDependencyInjector(GlobalContext)
    ) {
        if (provisionedState.value) {
            val members = remember {
                mutableStateOf(
                    persistentListOf(
                        TeamMember(
                            "dokka",
                            ValorantAgentIdentity.NEON.uuid,
                            "1",
                            100,
                            false
                        ),
                        TeamMember(
                            "dex",
                            ValorantAgentIdentity.CHAMBER.uuid,
                            "2",
                            101,
                            false
                        ),
                        TeamMember(
                            "moon",
                            ValorantAgentIdentity.JETT.uuid,
                            "3",
                            102,
                            false
                        ),
                        TeamMember(
                            "hive",
                            ValorantAgentIdentity.KAYO.uuid,
                            "4",
                            103,
                            false
                        ),
                        TeamMember(
                            "lock",
                            ValorantAgentIdentity.OMEN.uuid,
                            "5",
                            104,
                            false
                        )
                    )
                )
            }
            LiveInGameTeamMembersColumn(
                modifier = Modifier.background(Material3Theme.backgroundColorAsState().value),
                matchKey = remember { Any() },
                user = remember { "dokka" },
                loading = false,
                membersProvided = true,
                getMembers = { members.value }
            )
        }
    }
}
