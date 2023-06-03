package dev.flammky.valorantcompanion.live.match.presentation.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class UserMatchPresenter {

    @Composable
    fun present(): UserMatchInfoUIState {
        val localProducer = remember {
            UserMatchUIStateProducer()
        }
        return localProducer.produce()
    }

    private class UserMatchUIStateProducer() {

        @Composable
        fun produce(): UserMatchInfoUIState {
            TODO()
        }
    }
}