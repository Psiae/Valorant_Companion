package dev.flammky.valorantcompanion.live.store.presentation.agent

import android.util.Log
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.base.ProjectTree
import dev.flammky.valorantcompanion.base.checkInMainLooper
import dev.flammky.valorantcompanion.base.compose.RememberObserver
import dev.flammky.valorantcompanion.base.di.DependencyInjector
import dev.flammky.valorantcompanion.base.di.requireInject
import dev.flammky.valorantcompanion.base.kt.cast
import dev.flammky.valorantcompanion.base.kt.coroutines.awaitOrCancelOnException
import dev.flammky.valorantcompanion.live.BuildConfig
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreService
import dev.flammky.valorantcompanion.pvp.store.ValorantUserStoreClient
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.*
import kotlin.coroutines.Continuation

interface AgentStoreScreenPresenter

@Composable
fun rememberAgentStoreScreenPresenter(
    di: DependencyInjector
): AgentStoreScreenPresenter {
    val storeService: ValorantStoreService = di.requireInject()
    val assetsService: ValorantAssetsService = di.requireInject()
    return remember(storeService, assetsService) {
        AgentStoreScreenPresenterImpl(assetsService, storeService)
    }
}

@Composable
fun AgentStoreScreenPresenter.present(
    di: DependencyInjector
): AgentStoreScreenState {
    return present(
        authRepository = di.requireInject()
    )
}

@Composable
fun AgentStoreScreenPresenter.present(
    userUUID: String
): AgentStoreScreenState {
    return cast<AgentStoreScreenPresenterImpl>()
        .present(userUUID = userUUID)
}

@Composable
fun AgentStoreScreenPresenter.present(
    authRepository: RiotAuthRepository
): AgentStoreScreenState {
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
        return AgentStoreScreenState.UNSET
    }
    return present(
        userUUID = activeAccountState.value?.model?.id ?: "",
    )
}

private class AgentStoreScreenPresenterImpl(
    private val assetsService: ValorantAssetsService,
    private val storeService: ValorantStoreService
) : AgentStoreScreenPresenter {

    @Composable
    fun present(
        userUUID: String
    ): AgentStoreScreenState {
        val producer = remember(this, userUUID) {
            StateProducer(userUUID)
        }.apply {
            SideEffect {
                produceParams()
            }
        }
        return producer.readSnapshot()
    }


    private inner class StateProducer(
        private val userUUID: String
    ) : RememberObserver {

        private val _state = mutableStateOf<AgentStoreScreenState?>(
            null,
            neverEqualPolicy()
        )

        private var remembered = false
        private var forgotten = false
        private var abandoned = false
        private var initialProduce = true
        private val lifetime = SupervisorJob()
        private var _coroutineScope: CoroutineScope? = null
        private var _assetLoader: ValorantAssetsLoaderClient? = null
        private var _storeClient: ValorantUserStoreClient? = null

        private var pendingRefreshContinuations = mutableListOf<Continuation<Unit>>()

        private var producer: Job? = null

        private val producing
            get() = producer?.isActive == true

        private val coroutineScope
            get() = _coroutineScope!!

        private val assetLoader
            get() = _assetLoader!!

        private val storeClient
            get() = _storeClient!!

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
            assetLoader.dispose()
            storeClient.dispose()
        }

        override fun onRemembered() {
            super.onRemembered()
            check(!remembered)
            check(!forgotten)
            check(!abandoned)
            remembered = true
            _coroutineScope = CoroutineScope(Dispatchers.Main + lifetime)
            _assetLoader = assetsService.createLoaderClient()
            _storeClient = storeService.createClient(userUUID)
        }

        fun produceParams(

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
            if (initialProduce) {
                initialProduce = false
                onInitialProduce()
            }
        }

        fun readSnapshot(): AgentStoreScreenState {
            return stateValueOrUnset()
        }

        private fun onInitialProduce() {
            mutateState("onInitialProduce") { state ->
                state.UNSET
            }
            check(!producing)
            producer = produceState()
        }

        private fun produceState(): Job {
            return coroutineScope.launch {
                listOf(
                    produceLiveAgents(),
                    produceEntitledAgents()
                ).joinAll()
            }
        }

        private fun produceLiveAgents(): Job {
            return coroutineScope.launch {
                assetLoader
                    .agentAssetLoader
                    .loadLiveAgentsUUIDsAsync()
                    .awaitOrCancelOnException()
                    .fold(
                        onSuccess = { set ->
                            mutateState("produceLiveAgents_success") { state ->
                                state.copy(
                                    agents = set,
                                    validAgents = true
                                )
                            }
                        },
                        onFailure = { ex ->
                            mutateState("produceLiveAgents_failure") { state ->
                                state.copy(
                                    agents = state.UNSET.agents,
                                    validAgents = false
                                )
                            }
                        }
                    )
            }
        }

        private fun produceEntitledAgents(): Job {
            return coroutineScope.launch {
                storeClient
                    .fetchEntitledAgent()
                    .apply {
                        init()
                    }
                    .asDeferred()
                    .awaitOrCancelOnException()
                    .fold(
                        onSuccess = { agents ->
                            mutateState("produceEntitledAgents_success") { state ->
                                state.copy(
                                    entitledAgents = agents.toImmutableSet(),
                                    validEntitledAgents = true
                                )
                            }
                        },
                        onFailure = { ex ->
                            Log.d("DEBUG", "$ex")
                            mutateState("produceEntitledAgents_failure") { state ->
                                state.copy(
                                    entitledAgents = state.UNSET.entitledAgents,
                                    validEntitledAgents = true
                                )
                            }
                        }
                    )
            }
        }

        private fun stateValueOrUnset(): AgentStoreScreenState {
            return _state.value ?: AgentStoreScreenState.UNSET
        }

        private fun mutateState(
            action: String,
            mutate: (AgentStoreScreenState) -> AgentStoreScreenState
        ) {
            checkInMainLooper()
            val current = stateValueOrUnset()
            val new = mutate(current)
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "${ProjectTree.packageName}.AgentStoreScreenPresenterKt: StateProducer_mutateState($action), result=$new"
            )
            if (current === new) return
            _state.value = new
        }
    }
}