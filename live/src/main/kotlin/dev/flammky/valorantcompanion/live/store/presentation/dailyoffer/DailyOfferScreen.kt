package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.requireInject

@Composable
fun DailyOfferScreen(
    isVisibleToUser: Boolean
) {
    DailyOfferScreenContent(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        dailyOfferState = rememberDailyOfferScreenPresenter(di = LocalDependencyInjector.current)
            .present(
                authRepository = LocalDependencyInjector.current.requireInject(),
                isVisibleToUser = isVisibleToUser
            )
    )
}