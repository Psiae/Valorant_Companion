package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import androidx.compose.runtime.Composable
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.requireInject

@Composable
fun DailyOffer(
    isVisibleToUser: Boolean
) {
    DailyOfferScreenContent(
        dailyOfferState = rememberDailyOfferScreenPresenter(di = LocalDependencyInjector.current)
            .present(
                authRepository = LocalDependencyInjector.current.requireInject(),
                isVisibleToUser = isVisibleToUser
            )
    )
}