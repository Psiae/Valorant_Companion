package dev.flammky.valorantcompanion.live.loadout.presentation.root

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.flammky.valorantcompanion.live.loadout.presentation.SprayLoadoutScreen
import dev.flammky.valorantcompanion.live.loadout.presentation.SprayLoadoutScreenState
import dev.flammky.valorantcompanion.live.main.LiveMainScreenScope

@Composable
internal fun LiveLoadout(
    modifier: Modifier = Modifier,
    openScreen: (@Composable LiveMainScreenScope.() -> Unit) -> Unit
) {
    LiveLoadoutPlacement(
        modifier = modifier,
        surface = { LiveLoadoutSurface() },
        content = {
            LiveLoadoutContent(
                // TODO: impl
                weaponEnabled = false,
                openWeaponScreen = {
                    openScreen.invoke {
                        BackHandler(onBack = ::dismiss)
                    }
                },
                sprayEnabled = true,
                openSprayScreen = {
                    openScreen.invoke {
                        BackHandler(onBack = ::dismiss)
                        SprayLoadoutScreen(
                            modifier = Modifier,
                            state = // TODO
                            remember {
                                SprayLoadoutScreenState(

                                )
                            } ,
                            dismiss = {}
                        )
                    }
                },
            )
        }
    )
}

@Composable
private fun LiveLoadoutPlacement(
    modifier: Modifier,
    surface: @Composable () -> Unit,
    content: @Composable () -> Unit
) = Box(modifier = modifier.fillMaxSize()) {
    surface()
    content()
}