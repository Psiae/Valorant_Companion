package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import android.util.Log
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.base.checkInMainLooper
import dev.flammky.valorantcompanion.base.compose.RememberObserver
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead
import dev.flammky.valorantcompanion.base.di.DependencyInjector
import dev.flammky.valorantcompanion.base.di.requireInject
import dev.flammky.valorantcompanion.base.kt.cast
import dev.flammky.valorantcompanion.base.kt.coroutines.awaitOrCancelOnException
import dev.flammky.valorantcompanion.base.loop
import dev.flammky.valorantcompanion.live.BuildConfig
import dev.flammky.valorantcompanion.pvp.store.StoreFrontData
import dev.flammky.valorantcompanion.pvp.store.ValorantUserStoreClient
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

interface DailyOfferScreenPresenter {

    @Composable
    fun present(user: String, isVisibleToUser: Boolean): DailyOfferScreenState
}

@Composable
fun rememberDailyOfferScreenPresenter(
    di: DependencyInjector
): DailyOfferScreenPresenter {
    return rememberDailyOfferScreenPresenter(storeService = di.requireInject())
}

@Composable
fun rememberDailyOfferScreenPresenter(
    storeService: ValorantStoreService
): DailyOfferScreenPresenter {
    return remember(storeService) {
        DailyOfferScreenPresenterImpl(
            storeService
        )
    }
}



private class DailyOfferScreenPresenterImpl(
    private val storeService: ValorantStoreService,
) : DailyOfferScreenPresenter {


    @Composable
    override fun present(user: String, isVisibleToUser: Boolean): DailyOfferScreenState {
        val producer = remember(this, user) {
            StateProducer(user)
        }
        return producer
            .apply {
                SideEffect {
                    produce(isVisibleToUser)
                }
            }
            .readSnapshot()
    }

    private inner class StateProducer(
        private val user: String
    ): RememberObserver {

        private val _state = mutableStateOf<DailyOfferScreenState?>(null)

        private var remembered = false
        private var forgotten = false
        private var abandoned = false
        private var initialProduce = true
        private val lifetime = SupervisorJob()
        private var _coroutineScope: CoroutineScope? = null
        private var _storeClient: ValorantUserStoreClient? = null

        private var producer: Job? = null

        private val isVisibleToUserState = mutableStateOf(false)

        private val coroutineScope
            get() = _coroutineScope!!

        private val producing
            get() = producer?.isActive == true

        private val storeClient
            get() = _storeClient!!

        private var _suspension: CompletableJob? = null

        private val suspension: Job
            get() = _suspension!!

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

        fun produce(
            isVisibleToUser: Boolean,
        ) {
            checkInMainLooper() {
                "produce must be called on the MainThread, " +
                        "make sure this function is called within a composable side-effect block"
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
                isVisibleToUser,
            )
        }


        @SnapshotRead
        fun readSnapshot(): DailyOfferScreenState {
            return stateValueOrUnset()
        }

        private fun stateValueOrUnset(): DailyOfferScreenState {
            return _state.value ?: DailyOfferScreenState.UNSET
        }

        private fun invalidateParams(
            isVisibleToUser: Boolean,
        ) {
            if (initialProduce) {
                initialProduce = false
                onInitialProduce()
            }
            isVisibleToUserState.value = isVisibleToUser
        }

        private fun onInitialProduce() {
            producer = produceState()
        }

        private fun produceState(): Job {
            mutateState("produceState") { state ->
                state.UNSET
            }
            return coroutineScope.launch {
                loop {
                    snapshotFlow { isVisibleToUserState.value }.first { it }
                    if (!initFetchStoreFrontData()) {
                        // TODO: ask refresh
                        suspension.join()
                        LOOP_CONTINUE()
                    }
                    delay(1000)
                }
            }
        }

        private suspend fun initFetchStoreFrontData(): Boolean {
            storeClient
                .fetchStoreFrontAsync()
                .awaitOrCancelOnException()
                .fold(
                    onSuccess = { data ->
                        onFetchStoreFrontDataSuccess(data)
                        return true
                    },
                    onFailure = { ex ->
                        onFetchStoreFrontDataFailure(ex.cast())
                        return false
                    }
                )
        }

        private fun onFetchStoreFrontDataSuccess(
            data: StoreFrontData
        ) {
            if (BuildConfig.DEBUG) {
                Log.d(
                    BuildConfig.LIBRARY_PACKAGE_NAME,
                    "live.store.presentation.dailyoffer.DailyOfferScreenPresenter::StateProducer_onFetchStoreFrontDataSuccess()"
                )
            }
            mutateState("onFetchStoreFrontDataSuccess") { state ->
                state.copy(
                    featuredBundle = data.featuredBundleStore,
                    skinsPanel = data.skinsPanel,
                    accessory = data.accessoryStore
                )
            }
        }

        private fun onFetchStoreFrontDataFailure(
            ex: Exception
        ) {
            if (BuildConfig.DEBUG) {
                Log.d(
                    BuildConfig.LIBRARY_PACKAGE_NAME,
                    "live.store.presentation.dailyoffer.DailyOfferScreenPresenter::StateProducer_onFetchStoreFrontDataFailure($ex)"
                )
            }
            check(_suspension == null) {
                "onFetchStoreFrontDataFailure with suspension present"
            }
            _suspension = Job(lifetime)
            mutateState("onFetchFailure") { state ->
                state.UNSET.copy(
                    needManualRefresh = true,
                    needManualRefreshMessage = "UNABLE TO FETCH STOREFRONT DATA",
                    manualRefresh = { _suspension?.complete() ; _suspension = null }
                )
            }
        }

        private fun mutateState(
            action: String,
            mutate: (DailyOfferScreenState) -> DailyOfferScreenState
        ) {
            checkInMainLooper()
            val current = stateValueOrUnset()
            val new = mutate(current)
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "live.store.presentation.dailyoffer.DailyOfferScreenPresenter: StateProducer_mutateState($action), result=$new".chunked(4000).joinToString("\n")
            )
            _state.value = new
        }
    }
}

@Composable
fun DailyOfferScreenPresenter.present(
    authRepository: RiotAuthRepository,
    isVisibleToUser: Boolean
): DailyOfferScreenState {
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
        return DailyOfferScreenState.UNSET
    }
    return present(
        user = activeAccountState.value?.model?.id ?: "",
        isVisibleToUser = isVisibleToUser
    )
}