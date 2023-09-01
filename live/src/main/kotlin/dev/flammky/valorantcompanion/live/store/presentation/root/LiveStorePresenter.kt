package dev.flammky.valorantcompanion.live.store.presentation.root

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
import dev.flammky.valorantcompanion.base.di.requireInject
import dev.flammky.valorantcompanion.base.loop
import dev.flammky.valorantcompanion.live.BuildConfig
import dev.flammky.valorantcompanion.live.pvp.match.presentation.root.UserMatchInfoUIState
import dev.flammky.valorantcompanion.pvp.store.StoreFrontData
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreClient
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlin.coroutines.coroutineContext

@Composable
internal fun rememberLiveStorePresenter(
    di: DependencyInjector,
): LiveStorePresenter {
    return rememberLiveStorePresenter(storeService = di.requireInject())
}

@Composable
internal fun rememberLiveStorePresenter(
    storeService: ValorantStoreService,
): LiveStorePresenter {
    return remember(storeService) { LiveStorePresenter(storeService) }
}

internal class LiveStorePresenter(
    private val storeService: ValorantStoreService
) {

    @Composable
    fun present(
        user: String,
        isVisibleToUser: Boolean
    ): LiveStoreState {
        return remember(user) {
            StateProducer(user)
        }.apply {
            SideEffect {
                produce(isVisibleToUser)
            }
        }.readSnapshot()
    }

    private inner class StateProducer(
        private val user: String
    ) : RememberObserver {

        private var _state = mutableStateOf<LiveStoreState?>(null)

        private var remembered = false
        private var forgotten = false
        private var abandoned = false
        private var initialProduce = true
        private val lifetime = SupervisorJob()
        private var _coroutineScope: CoroutineScope? = null
        private var _storeClient: ValorantStoreClient? = null

        private var producer: Job? = null

        private val producing
            get() = producer?.isActive == true

        private val coroutineScope
            get() = _coroutineScope!!

        private val storeClient
            get() = _storeClient!!

        private val isVisibleToUser = mutableStateOf(false)

        @MainThread
        fun produce(
            isVisibleToUser: Boolean
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
                isVisibleToUser
            )
        }

        private fun invalidateParams(
            isVisibleToUser: Boolean
        ) {
            if (initialProduce) {
                initialProduce = false
                onInitialProduce()
            }
            this.isVisibleToUser.value = isVisibleToUser
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
                    snapshotFlow { isVisibleToUser.value }.first { it }
                    fetchData()
                    delay(1000)
                }
            }
        }

        private suspend fun fetchData() {
            coroutineContext.ensureActive()
            val def = storeClient.fetchDataAsync()
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
            data: StoreFrontData
        ) {
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "live.store.presentation.LiveStorePresenterKt: StateProducer_onFetchSuccess()"
            )
            checkInMainLooper()
            mutateState("onFetchSuccess") { state ->
                state.copy(
                    dailyOfferEnabled = data.featuredBundle.open,
                    nightMarketEnabled = data.bonusStore.open,
                    accessoriesEnabled = data.accessoryStore.open,
                    agentsEnabled = true
                )
            }
        }

        private fun onFetchFailure(
            ex: Exception,
            // TODO: error code: Int
        ) {
            checkInMainLooper()
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "live.loadout.presentation.SprayLoadoutPickerPresenterKt: StateProducer_onFetchFailure($ex)"
            )
            mutateState("onFetchFailure") { state ->
                state.copy(
                    dailyOfferEnabled = false,
                    nightMarketEnabled = false,
                    accessoriesEnabled = false,
                    agentsEnabled = false
                )
            }
        }

        @SnapshotRead
        fun readSnapshot(): LiveStoreState {
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
            storeClient.dispose()
        }

        override fun onRemembered() {
            super.onRemembered()
            check(!remembered)
            check(!forgotten)
            check(!abandoned)
            remembered = true
            _coroutineScope = CoroutineScope(Dispatchers.Main + lifetime)
            _storeClient = storeService.createClient(user)
        }

        private fun stateValueOrUnset(): LiveStoreState {
            return _state.value ?: LiveStoreState.UNSET
        }

        private fun mutateState(
            action: String,
            mutate: (LiveStoreState) -> LiveStoreState
        ) {
            checkInMainLooper()
            val current = stateValueOrUnset()
            val new = mutate(current)
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "live.store.presentation.root.LiveStorePresenterKt: StateProducer_mutateState($action), result=$new"
            )
            _state.value = new
        }
    }
}

@Composable
internal fun LiveStorePresenter.present(
    isVisibleToUser: Boolean,
    authRepository: RiotAuthRepository
): LiveStoreState {

    val activeAccountState = remember(authRepository) {
        mutableStateOf<AuthenticatedAccount?>(null)
    }
    val initialized = remember(authRepository) {
        mutableStateOf(false)
    }
    DisposableEffect(
        authRepository,
    ) {
        val listener = ActiveAccountListener { old, new ->
            activeAccountState.value = new
            initialized.value = true
        }
        authRepository.registerActiveAccountChangeListener(
            listener
        )
        onDispose {
            authRepository.unRegisterActiveAccountListener(listener)
        }
    }
    if (!initialized.value) {
        return LiveStoreState.UNSET
    }
    return present(
       user = activeAccountState.value?.model?.id ?: "",
        isVisibleToUser = isVisibleToUser
    )
}