package dev.flammky.valorantcompanion.live.store.presentation.nightmarket

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector

@Composable
internal fun NightMarketScreen(
    state: NightMarketScreenState
) {
    NightMarketScreenContent(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        state = state
    )
}