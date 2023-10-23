package dev.flammky.valorantcompanion.live.store.presentation.root

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.flammky.valorantcompanion.base.compose.rememberUpdatedStateWithCustomEquality
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.requireInject
import dev.flammky.valorantcompanion.base.referentialEqualityFun
import dev.flammky.valorantcompanion.base.rememberWithEquality
import dev.flammky.valorantcompanion.live.main.LiveMainScreenScope
import dev.flammky.valorantcompanion.live.store.presentation.agent.AgentStoreScreen
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.DailyOfferScreen
import dev.flammky.valorantcompanion.live.store.presentation.nightmarket.NightMarketScreen
import dev.flammky.valorantcompanion.live.store.presentation.nightmarket.NightMarketScreenState

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
    val upState = rememberUpdatedStateWithCustomEquality(
        key = state,
        equality = referentialEqualityFun()
    ) {
        state
    }
    LiveStorePlacement(
        modifier = modifier,
        surface = { LiveStoreSurface(modifier = Modifier) },
        content = {
            LiveStoreContent(
                modifier = Modifier,
                dailyOfferEnabled = state.dailyOfferEnabled,
                openDailyOffer = {
                    openScreen.invoke {
                        DailyOfferScreen(isVisibleToUser = hasFocus)
                    }
                },
                nightMarketOpen = state.nightMarketEnabled,
                openNightMarket = {
                    openScreen.invoke {
                        val bonusStore = upState.value.bonusStore
                        NightMarketScreen(
                            state = rememberWithEquality(
                                key = state,
                                keyEquality = referentialEqualityFun()
                            ) {
                                NightMarketScreenState(bonusStore)
                            }
                        )
                    }
                },
                agentEnabled = state.agentsEnabled,
                openAgent = {
                    openScreen.invoke {
                        AgentStoreScreen(
                            isVisibleToUser = hasFocus
                        )
                    }
                }
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