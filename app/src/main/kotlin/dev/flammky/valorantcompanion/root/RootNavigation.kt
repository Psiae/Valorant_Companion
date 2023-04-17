package dev.flammky.valorantcompanion.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.flammky.valorantcompanion.boarding.MainBoarding
import dev.flammky.valorantcompanion.boarding.MainBoardingPresenter

@Composable
fun RootNavigation(
    state: RootNavigationState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ComposeMainBoarding(state)
    }
}

@Composable
private fun ComposeMainBoarding(
    state: RootNavigationState
) {
    if (/*state.showBoarding*/ true) {
        MainBoarding(state = remember { MainBoardingPresenter() }.present())
    }
}