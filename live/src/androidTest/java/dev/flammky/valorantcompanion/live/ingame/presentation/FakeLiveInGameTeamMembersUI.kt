package dev.flammky.valorantcompanion.live.ingame.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.debug.DebugValorantAssetService
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.koin.compose.KoinDependencyInjector
import dev.flammky.valorantcompanion.pvp.TeamID
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import dev.flammky.valorantcompanion.pvp.mmr.ValorantMMRService
import dev.flammky.valorantcompanion.pvp.player.ValorantNameService
import dev.flammky.valorantcompanion.pvp.mmr.debug.StubValorantMMRService
import dev.flammky.valorantcompanion.pvp.mmr.debug.StubValorantNameService
import kotlinx.collections.immutable.persistentListOf
import org.koin.core.context.GlobalContext
import org.koin.dsl.module

@Composable
@Preview
private fun FakeLiveInGameTeamMembersUIPreview() {
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
                            StubValorantNameService(map = StubValorantNameService.DEFAULT_FAKE_NAMES)
                        }
                        single<ValorantMMRService> {
                            StubValorantMMRService(provider = StubValorantMMRService.DEFAULT_FAKE_PROVIDER)
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
            LiveInGameTeamMembersUI(
                user = "dokka",
                matchKey = remember { Any() },
                loading = true,
                allyProvided = true,
                enemyProvided = true,
                ally = remember {
                    dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam(
                        id = TeamID.BLUE,
                        members = persistentListOf(
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
                },
                enemy = remember {
                    dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam(
                        id = TeamID.BLUE,
                        members = persistentListOf(
                            TeamMember(
                                "lock",
                                ValorantAgentIdentity.OMEN.uuid,
                                "5",
                                104,
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
                                "moon",
                                ValorantAgentIdentity.JETT.uuid,
                                "3",
                                102,
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
                                "dokka",
                                ValorantAgentIdentity.NEON.uuid,
                                "1",
                                100,
                                false
                            ),
                        )
                    )
                }
            )
        }
    }
}