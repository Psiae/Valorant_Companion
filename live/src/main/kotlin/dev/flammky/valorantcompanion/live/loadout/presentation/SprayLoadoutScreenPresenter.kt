package dev.flammky.valorantcompanion.live.loadout.presentation

import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.flammky.valorantcompanion.base.checkInMainLooper
import dev.flammky.valorantcompanion.base.compose.BaseRememberObserver
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead
import dev.flammky.valorantcompanion.base.loop
import dev.flammky.valorantcompanion.live.BuildConfig
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadout
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadoutClient
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadoutService
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

class SprayLoadoutScreenPresenter(
    private val loadoutService: PlayerLoadoutService
) {

    @Composable
    fun present(
        user: String
    ): SprayLoadoutScreenState {
        return remember(this, user) {
            StateProducer(user)
        }.apply {
            SideEffect {
                produce()
            }
        }.readSnapshot()
    }

    private inner class StateProducer(
        private val user: String
    ): BaseRememberObserver {

        private var _state = mutableStateOf<SprayLoadoutScreenState?>(null)

        private var remembered = false
        private var forgotten = false
        private var abandoned = false
        private var initialProduce = false
        private val lifetime = SupervisorJob()
        private var _coroutineScope: CoroutineScope? = null
        private var _loadoutClient: PlayerLoadoutClient? = null

        private var producer: Job? = null

        private val producing
            get() = producer?.isActive == true

        private val coroutineScope
            get() = _coroutineScope!!

        private val loadoutClient
            get() = _loadoutClient!!

        @MainThread
        fun produce(
            
        ) {
            checkInMainLooper() {
                "produce must be called on the MainThread, " +
                        "make sure this function is called within a side-effect block"
            }
            check(remembered) {
                "StateProducer must be remembered before calling produce, " +
                        "expected for compose runtime to invoke remember observer before side-effects"
            }
            check(!forgotten) {
                "StateProducer must not be forgotten before calling produce" +
                        "expected for compose runtime to not invoke side-effects when forgotten"
            }
            invalidateParams(

            )
        }
        
        private fun invalidateParams(
            
        ) {
            if (initialProduce) {
                initialProduce = false
                onInitialProduce()
                return
            }
        }

        private fun onInitialProduce() {
            producer = produceState()
        }

        private fun produceState(): Job {
            return coroutineScope.launch {
                mutateState("produceState") { state ->
                    state.UNSET
                }
                loop {
                    fetchData()
                }
            }
        }

        // TODO:
        private suspend fun fetchData() {
            awaitCancellation()
        }

        private fun onFetchSuccess(
            data: PlayerLoadout
        ) {

        }

        @SnapshotRead
        fun readSnapshot(): SprayLoadoutScreenState {
            return stateValueOrUnset()
        }

        override fun onAbandoned() {
            super.onAbandoned()
            check(!remembered)
            check(!forgotten)
            check(!abandoned)
            abandoned = true
        }

        override fun onForgotten() {
            super.onForgotten()
            check(remembered)
            check(!forgotten)
            check(!abandoned)
            forgotten = true
            lifetime.cancel()
            coroutineScope.cancel()
            loadoutClient.dispose()
        }

        override fun onRemembered() {
            super.onRemembered()
            check(!remembered)
            check(!forgotten)
            check(!abandoned)
            remembered = true
            _coroutineScope = CoroutineScope(Dispatchers.Main + lifetime)
            _loadoutClient = loadoutService.createClient()
        }

        private fun stateValueOrUnset(): SprayLoadoutScreenState {
            return _state.value ?: SprayLoadoutScreenState.UNSET
        }

        private fun mutateState(
            action: String,
            mutate: (SprayLoadoutScreenState) -> SprayLoadoutScreenState
        ) {
            checkInMainLooper()
            val current = stateValueOrUnset()
            val new = mutate(current)
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "live.loadout.presentation.SprayLoadoutScreenPresenterKt: StateProducer_mutateState($action), result=$new"
            )
            _state.value = new
        }
    }
}