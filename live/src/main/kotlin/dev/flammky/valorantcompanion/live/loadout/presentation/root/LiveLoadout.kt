package dev.flammky.valorantcompanion.live.loadout.presentation.root

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.flammky.valorantcompanion.live.main.LiveMainScreenContainer

@Composable
internal fun LiveLoadout(
    modifier: Modifier = Modifier,
    openScreen: (@Composable LiveMainScreenContainer.() -> Unit) -> Unit
) {
    LiveLoadoutPlacement(
        modifier = modifier,
        surface = { LiveLoadoutSurface() },
        content = {
            LiveLoadoutContent(
                openSprayScreen = {
                    openScreen.invoke {
                        BackHandler(onBack = ::dismiss)
                    }
                },
                openWeaponScreen = {
                    openScreen.invoke {
                        BackHandler(onBack = ::dismiss)
                    }
                }
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