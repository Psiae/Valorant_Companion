package dev.flammky.valorantcompanion.live.pvp.mission

import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead

class LiveMissionScreenPresenter() {


    @Composable
    fun present(): LiveMissionScreenState {
        val producer = remember(this) { StateProducer() }.apply {
            SideEffect {
                produce()
            }
        }
        return producer.snapshot()
    }

    private class StateProducer() {

        private val _state = mutableStateOf<LiveMissionScreenState?>(null)

        @MainThread
        fun produce() {

        }

        @SnapshotRead
        fun snapshot(): LiveMissionScreenState {
            return stateValueOrUnset()
        }

        @SnapshotRead
        private fun stateValueOrUnset(): LiveMissionScreenState {
           return _state.value ?: LiveMissionScreenState.UNSET
        }
    }
}