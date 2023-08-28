package dev.flammky.valorantcompanion.live.loadout.presentation

import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.base.checkInMainLooper
import dev.flammky.valorantcompanion.base.compose.RememberObserver
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead
import dev.flammky.valorantcompanion.base.di.DependencyInjector
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.requireInject
import dev.flammky.valorantcompanion.base.loop
import dev.flammky.valorantcompanion.live.BuildConfig
import dev.flammky.valorantcompanion.pvp.loadout.PlayerAvailableSprayLoadout
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadoutClient
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadoutService
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

@Composable
internal fun rememberSprayLoadoutPickerPoolPresenter(
    di: DependencyInjector = LocalDependencyInjector.current
): SprayLoadoutPickerPoolPresenter {
    return rememberSprayLoadoutPickerPoolPresenter(
        authRepository = di.requireInject(),
        loadoutService = di.requireInject()
    )
}

@Composable
internal fun rememberSprayLoadoutPickerPoolPresenter(
    authRepository: RiotAuthRepository,
    loadoutService: PlayerLoadoutService
): SprayLoadoutPickerPoolPresenter {
    return remember(loadoutService, authRepository) {
        SprayLoadoutPickerPoolPresenter(
            authRepository = authRepository,
            loadoutService = loadoutService
        )
    }
}


internal class SprayLoadoutPickerPoolPresenter(
    private val authRepository: RiotAuthRepository,
    private val loadoutService: PlayerLoadoutService
) {

    @Composable
    fun present(): SprayLoadoutPickerPoolState {
        val activeAccountState = remember(this) {
            mutableStateOf<AuthenticatedAccount?>(null)
        }
        val loaded = remember(this) {
            mutableStateOf<Boolean>(false)
        }
        DisposableEffect(
            this,
        ) {
            val listener = ActiveAccountListener { old, new ->
                loaded.value = true
                activeAccountState.value = new
            }
            authRepository.registerActiveAccountChangeListener(
                listener
            )
            onDispose {
                authRepository.unRegisterActiveAccountListener(listener)
            }
        }
        if (!loaded.value) {
            return SprayLoadoutPickerPoolState.UNSET
        }
        return activeAccountState.value
            ?.let { account -> present(account.model.id) }
            ?: SprayLoadoutPickerPoolState.UNSET
    }

    @Composable
    fun present(
        user: String
    ): SprayLoadoutPickerPoolState {
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
    ) : RememberObserver {
        private val _state = mutableStateOf<SprayLoadoutPickerPoolState?>(null)

        private var remembered = false
        private var forgotten = false
        private var abandoned = false
        private var initialProduce = true
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

        @SnapshotRead
        fun readSnapshot(): SprayLoadoutPickerPoolState {
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
                    delay(1000)
                }
            }
        }

        private suspend fun fetchData() {
            coroutineContext.ensureActive()
            val def = loadoutClient.fetchPlayerAvailableSprayLoadoutAsync(user)
            val result = runCatching { def.await() }
                .onFailure { def.cancel() }
                .getOrThrow()
            coroutineContext.ensureActive()
            result
                .onFailure { ex ->
                    onFetchFailure(ex as Exception)
                }.onSuccess { data ->
                    onFetchSuccess(data)
                }
        }

        private fun onFetchSuccess(
            data: PlayerAvailableSprayLoadout
        ) {
            mutateState("onFetchSuccess") { state ->
                state.copy(
                    ownedSpraysKey = Any(),
                    ownedSprays = data.sprays
                )
            }
        }

        private fun onFetchFailure(
            ex: Exception,
            // TODO: error code: Int
        ) {
            mutateState("onFetchFailure") { state ->
                state.copy(
                    ownedSpraysKey = Any(),
                    ownedSprays = persistentListOf()
                )
            }
        }

        private fun stateValueOrUnset(): SprayLoadoutPickerPoolState = _state.value
            ?: SprayLoadoutPickerPoolState.UNSET

        private fun mutateState(
            action: String,
            mutate: (SprayLoadoutPickerPoolState) -> SprayLoadoutPickerPoolState
        ) {
            checkInMainLooper()
            val current = stateValueOrUnset()
            val new = mutate(current)
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "live.loadout.presentation.SprayLoadoutPickerPoolPresenterKt: StateProducer_mutateState($action), result=$new"
            )
            _state.value = new
        }
    }
}