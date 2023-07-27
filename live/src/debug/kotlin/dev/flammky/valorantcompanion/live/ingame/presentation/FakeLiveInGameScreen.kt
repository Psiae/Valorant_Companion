package dev.flammky.valorantcompanion.live.ingame.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.debug.DebugValorantAssetService
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.koin.compose.KoinDependencyInjector
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.pvp.mmr.ValorantMMRService
import dev.flammky.valorantcompanion.pvp.player.ValorantNameService
import dev.flammky.valorantcompanion.pvp.mmr.debug.StubValorantMMRService
import dev.flammky.valorantcompanion.pvp.mmr.debug.StubValorantNameService
import org.koin.core.context.GlobalContext
import org.koin.dsl.module

@Composable
@Preview
private fun FakeLiveInGameScreenPreview() = DefaultMaterial3Theme(
    dynamic = true,
    dark = isSystemInDarkTheme(),
    content = {
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
            if (provisionedState.value) FakeLiveInGameScreen()
        }
    }
)

@Composable
internal fun FakeLiveInGameScreen() = FakeLiveInGamePlacement(
    modifier = Modifier,
    background = { backgroundModifier -> FakeLiveInGameBackground(backgroundModifier) },
    content = { contentModifier -> FakeLiveInGameContent(contentModifier) }
)

@Composable
private inline fun FakeLiveInGamePlacement(
    modifier: Modifier,
    background: @Composable (Modifier) -> Unit,
    content: @Composable (Modifier) -> Unit
) = Box(modifier) {
    background(Modifier)
    content(Modifier)
}