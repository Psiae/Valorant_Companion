package dev.flammky.valorantcompanion.boarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class MainBoardingPresenter() {

    @Composable
    fun present(): MainBoardingState {
        return remember { MainBoardingState() }
    }
}