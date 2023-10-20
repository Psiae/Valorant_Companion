package dev.flammky.valorantcompanion.live.store.presentation.agent

import android.util.Log
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.base.ProjectTree
import dev.flammky.valorantcompanion.base.checkInMainLooper
import dev.flammky.valorantcompanion.base.compose.RememberObserver
import dev.flammky.valorantcompanion.base.di.DependencyInjector
import dev.flammky.valorantcompanion.base.di.requireInject
import dev.flammky.valorantcompanion.base.kt.coroutines.awaitOrCancelOnException
import dev.flammky.valorantcompanion.live.BuildConfig
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import kotlinx.coroutines.*
import kotlin.coroutines.Continuation


interface AgentDisplayCardPresenter

@Composable
fun AgentDisplayCardPresenter.present(
    agentUUID: String
): AgentDisplayCardState {
    this as AgentDisplayCardPresenterImpl
    return present(agentUUID)
}

@Composable
fun rememberAgentDisplayCardPresenter(
    dependencyInjector: DependencyInjector
): AgentDisplayCardPresenter {
    return remember(dependencyInjector) {
        AgentDisplayCardPresenterImpl(
            assetsService = dependencyInjector.requireInject()
        )
    }
}

private class AgentDisplayCardPresenterImpl(
    private val assetsService: ValorantAssetsService,
) : AgentDisplayCardPresenter {

    @Composable
    fun present(
        agentUUID: String
    ): AgentDisplayCardState {
        val producer = remember(this, agentUUID) {
            StateProducer(agentUUID)
        }.apply {
            SideEffect {
                produceParams()
            }
        }
        return producer.readSnapshot()
    }

    private inner class StateProducer(
        private val agentUUID: String
    ) : RememberObserver {

        private var remembered = false
        private var forgotten = false
        private var abandoned = false
        private var initialProduce = true
        private val lifetime = SupervisorJob()
        private var _coroutineScope: CoroutineScope? = null
        private var _assetLoader: ValorantAssetsLoaderClient? = null

        private var pendingRefreshContinuations = mutableListOf<Continuation<Unit>>()

        private var producer: Job? = null

        private val producing
            get() = producer?.isActive == true

        private val coroutineScope
            get() = _coroutineScope!!

        private val assetLoader
            get() = _assetLoader!!

        private val _state = mutableStateOf<AgentDisplayCardState?>(
            value = null,
            policy = neverEqualPolicy()
        )

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
        }

        override fun onRemembered() {
            super.onRemembered()
            check(!remembered)
            check(!forgotten)
            check(!abandoned)
            remembered = true
            _coroutineScope = CoroutineScope(Dispatchers.Main + lifetime)
            _assetLoader = assetsService.createLoaderClient()
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
            invalidateParams(
            )
        }

        fun readSnapshot(): AgentDisplayCardState {
            return snapshotOrUnset()
        }

        private fun snapshotOrUnset(): AgentDisplayCardState {
            return _state.value ?: AgentDisplayCardState.UNSET
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
                    produceAgentDisplayName(),
                    produceAgentImage(),
                    produceAgentRoleImage()
                ).joinAll()
            }
        }

        private fun produceAgentImage(): Job {
            return coroutineScope.launch {
                assetLoader
                    .loadAgentIconAsync(agentUUID)
                    .awaitOrCancelOnException()
                    .fold(
                        onSuccess = { img ->
                            mutateState("produceAgentImage_success") { state ->
                                state.copy(
                                    agentDisplayImageKey = Any(),
                                    agentDisplayImage = img
                                )
                            }
                        },
                        onFailure = {
                            mutateState("produceAgentImage_failure") { state ->
                                state.copy(
                                    agentDisplayImageKey = Any(),
                                    agentDisplayImage = state.UNSET.agentDisplayImage
                                )
                            }
                        }
                    )
            }
        }

        private fun produceAgentRoleImage(): Job {
            return coroutineScope.launch {
                ValorantAgentIdentity.ofID(agentUUID)
                    ?.let { identity ->
                        assetLoader
                            .loadAgentRoleIconAsync(identity.role.uuid)
                            .awaitOrCancelOnException()
                            .fold(
                                onSuccess = { img ->
                                    mutateState("produceAgentRoleImage_success") { state ->
                                        state.copy(
                                            agentRoleDisplayImageKey = Any(),
                                            agentRoleDisplayImage = img
                                        )
                                    }
                                },
                                onFailure = {
                                    mutateState("produceAgentRoleImage_failure") { state ->
                                        state.copy(
                                            agentRoleDisplayImageKey = Any(),
                                            agentRoleDisplayImage = state.UNSET.agentRoleDisplayImage
                                        )
                                    }
                                }
                            )
                    }
            }
        }

        private fun produceAgentDisplayName(): Job {
            return coroutineScope.launch {
                ValorantAgentIdentity.ofID(agentUUID)
                    ?.let { identity ->
                        mutateState("produceAgentDisplayName_success") { state ->
                            state.copy(
                                agentName = identity.displayName
                            )
                        }
                    }
                    ?: mutateState("produceAgentDisplayName_failure") { state ->
                        state.copy(
                            agentName = "UNKNOWN_AGENT_NAME"
                        )
                    }
            }
        }

        private fun invalidateParams() {

        }

        private fun mutateState(
            action: String,
            mutate: (AgentDisplayCardState) -> AgentDisplayCardState
        ) {
            checkInMainLooper()
            val current = snapshotOrUnset()
            val new = mutate(current)
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "${ProjectTree.packageName}.AgentDisplayCardPresenterKt: StateProducer_mutateState($action), result=$new"
            )
            if (current === new) return
            _state.value = new
        }
    }


}