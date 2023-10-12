package dev.flammky.valorantcompanion.live.store.presentation.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.requireInject
import dev.flammky.valorantcompanion.live.main.LiveMainScreenScope
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.DailyOffer

@Composable
fun LiveStore(
    modifier: Modifier,
    isVisibleToUser: Boolean,
    openScreen: (@Composable LiveMainScreenScope.() -> Unit) -> Unit
) {
    val state = rememberLiveStorePresenter(
        LocalDependencyInjector.current
    ).present(
        isVisibleToUser = isVisibleToUser,
        authRepository = LocalDependencyInjector.current.requireInject()
    )
    LiveStorePlacement(
        modifier = modifier,
        surface = { LiveStoreSurface(modifier = Modifier) },
        content = {
            LiveStoreContent(
                modifier = Modifier,
                dailyOfferEnabled = state.dailyOfferEnabled,
                openDailyOffer = {
                    openScreen.invoke { DailyOffer(isVisibleToUser = hasFocus) }
                },
                nightMarketOpen = state.nightMarketEnabled,
                openNightMarket = {},
                agentEnabled = state.agentsEnabled,
                openAgent = {}
            )
        }
    )
}


@Composable
private fun LiveStorePlacement(
    modifier: Modifier,
    surface: @Composable () -> Unit,
    content: @Composable () -> Unit
) = Box(modifier.fillMaxSize()) {
    surface()
    content()
}