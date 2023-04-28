package dev.flammky.valorantcompanion.root

import android.app.Activity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.flammky.valorantcompanion.MainActivity
import dev.flammky.valorantcompanion.base.runRemember
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundColorAsState
import dev.flammky.valorantcompanion.base.theme.material3.primaryColorAsState
import dev.flammky.valorantcompanion.base.theme.material3.surfaceColorAsState
import kotlin.math.ln

fun MainActivity.setRootContent() = setContent {
    ValorantCompanionTheme {
        ApplySystemUI()
        RootLayout()
    }
}

@Composable
private fun RootLayout() = RootLayoutPlacement(
    background = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Material3Theme.backgroundColorAsState().value)
                .pointerInput(Unit) {}
        )
    },
    navigation = {
        RootNavigation(
            state = rememberRootNavigationPresenter().present()
        )
    }
)

@Composable
private fun ApplySystemUI() {
    rememberSystemUiController().apply {
        setStatusBarColor(
            run {
                val tone = Material3Theme.primaryColorAsState().value
                val surface = Material3Theme.surfaceColorAsState().value
                val alpha = remember { ((4.5f * ln(x = 2f + 1)) + 2f) / 100f }
                remember(alpha, tone, surface) {
                    tone.copy(alpha = alpha).compositeOver(surface)
                }
            }
        )
        setNavigationBarColor(
            color = Color.Transparent
        )
        WindowCompat.setDecorFitsSystemWindows((LocalContext.current as Activity).window, false)
    }
}

@Composable
private fun RootLayoutPlacement(
    background: @Composable () -> Unit,
    navigation: @Composable () -> Unit
) = Box(modifier = Modifier.fillMaxSize()) {
    background()
    navigation()
}
