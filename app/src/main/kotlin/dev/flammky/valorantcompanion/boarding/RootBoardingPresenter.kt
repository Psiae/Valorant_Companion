package dev.flammky.valorantcompanion.boarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class RootBoardingPresenter() {

    @Composable
    fun present(): MainBoardingState {
        return remember { MainBoardingState() }
    }
}