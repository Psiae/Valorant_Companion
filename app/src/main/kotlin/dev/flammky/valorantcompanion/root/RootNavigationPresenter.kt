package dev.flammky.valorantcompanion.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember


class RootNavigationPresenter() {

    @Composable
    fun present(): RootNavigationState {
        return remember { RootNavigationState() }
    }
}