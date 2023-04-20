package dev.flammky.valorantcompanion.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.flammky.valorantcompanion.boarding.RootBoarding
import dev.flammky.valorantcompanion.boarding.RootBoardingPresenter
import dev.flammky.valorantcompanion.main.MainNavigation
import dev.flammky.valorantcompanion.main.rememberMainNavigationPresenter

@Composable
fun RootNavigation(
    state: RootNavigationState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        MainNavigation(state)
        RootBoarding(state)
    }
}

@Composable
private fun RootBoarding(
    state: RootNavigationState
) {
    if (state.showBoarding == true) {
        RootBoarding(state = remember { RootBoardingPresenter() }.present())
    }
}

@Composable
private fun MainNavigation(
    state: RootNavigationState
) {
    if (state.showBoarding == false) {
        MainNavigation(state = rememberMainNavigationPresenter().present())
    }
}