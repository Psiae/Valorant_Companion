package dev.flammky.valorantcompanion.live.pvp.pregame.presentation

import android.os.SystemClock
import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.base.*
import dev.flammky.valorantcompanion.base.compose.BaseRememberObserver
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead
import dev.flammky.valorantcompanion.base.di.compose.inject
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import dev.flammky.valorantcompanion.pvp.getOrThrow
import dev.flammky.valorantcompanion.pvp.mmr.*
import dev.flammky.valorantcompanion.pvp.player.GetPlayerNameRequest
import dev.flammky.valorantcompanion.pvp.player.ValorantNameService
import dev.flammky.valorantcompanion.pvp.season.ValorantSeasons
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.minutes

@Composable
fun rememberAgentSelectionPlayerCardPresenter(
    assetsService: ValorantAssetsService = inject(),
    nameService: ValorantNameService = inject(),
    mmrService: ValorantMMRService = inject(),
): AgentSelectionPlayerCardPresenter {
    return remember(assetsService, nameService) {
        AgentSelectionPlayerCardPresenter(assetsService, nameService, mmrService)
    }
}

class AgentSelectionPlayerCardPresenter(
    private val assetsService: ValorantAssetsService,
    private val nameService: ValorantNameService,
    private val mmrService: ValorantMMRService,
) {

    // TODO: error broadcaster, this way we can request refresh from the user
    @Composable
    fun present(
        user: String,
        player: PreGamePlayer,
        matchID: String,
    ): AgentSelectionPlayerCardState {
        return remember(user) { StateProducer(user) }.apply {
            SideEffect {
                produceParams(
                    player.puuid,
                    player.characterID,
                    player.identity.cardID,
                    player.identity.accountLevel,
                    player.identity.incognito,
                    player.characterSelectionState
                )
            }
        }.readSnapshot()
    }

    private inner class StateProducer(
        private val user: String
    ) : BaseRememberObserver {

        private val _state = mutableStateOf<AgentSelectionPlayerCardState?>(null, neverEqualPolicy())

        private var remembered = false
        private var forgotten = false
        private var abandoned = false

        private var _coroutineScope: CoroutineScope? = null
        private val coroutineScope get() = _coroutineScope!!

        private var _assetLoader: ValorantAssetsLoaderClient? = null
        private val assetLoader get() = _assetLoader!!

        private var _mmrClient: ValorantMMRUserClient? = null
        private val mmrClient get() = _mmrClient!!

        private var playerIdSupervisor: Job? = null

        private var playerId: String? = null
        private var playerAgentID: String? = null
        private var playerCardID: String? = null
        private var accountLevel: Int? = null
        private var incognito: Boolean? = null
        private var playerAgentSelectionState: CharacterSelectionState? = null

        @SnapshotRead
        fun readSnapshot(): AgentSelectionPlayerCardState = stateValueOrUnset()

        @MainThread
        fun produceParams(
            id: String,
            playerAgentID: String,
            playerCardID: String,
            accountLevel: Int,
            incognito: Boolean,
            agentSelectionState: CharacterSelectionState
        ) {
            checkInMainLooper()
            check(remembered)
            if (id != this.playerId) {
                newPlayerID(id)
            }
            playerDataParam(
                playerAgentID,
                agentSelectionState,
                playerCardID,
                accountLevel,
                incognito
            )
        }

        private fun newPlayerID(
            id: String,
        ) {
            playerIdSupervisor?.cancel()

            this.playerId = id

            mutateState("newPlayerID") { state ->
                state.copy(
                    isUser = user == id,
                    playerGameName = state.UNSET.playerGameName,
                    playerGameNameTag = state.UNSET.playerGameNameTag,
                    competitiveTierIcon = state.UNSET.competitiveTierIcon,
                    competitiveTierIconKey = state.UNSET.competitiveTierIconKey
                )
            }

            playerIdSupervisor = SupervisorJob()
            newPlayerIdSideEffect()
        }

        private fun playerDataParam(
            playerAgentID: String,
            playerAgentSelectionState: CharacterSelectionState,
            playerCardID: String,
            accountLevel: Int,
            incognito: Boolean
        ) {
            if (this.playerAgentID != playerAgentID) {
                newPlayerAgent(playerAgentID)
            }
            if (this.playerAgentSelectionState != playerAgentSelectionState) {
                newPlayerAgentSelectionState(playerAgentSelectionState)
            }
            if (this.playerCardID != playerCardID) {
                // TODO
            }
            if (this.accountLevel != accountLevel) {
                // TODO
            }
            if (this.incognito != incognito) {
                // TODO
            }
        }

        private fun newPlayerAgent(
            id: String
        ) {
            this.playerAgentID = id
            val identity = ValorantAgentIdentity.ofID(id)
            val icon = assetLoader.loadMemoryCachedAgentIcon(id)
            val selectedAgentRoleIcon = identity?.role?.let { it -> assetLoader.loadMemoryCachedRoleIcon(it.uuid) }
            mutateState("newPlayerAgent") { state ->
                state.copy(
                    hasSelectedAgent = true,
                    selectedAgentName = identity?.displayName ?: state.UNSET.selectedAgentName,
                    selectedAgentIcon = icon ?: state.UNSET.selectedAgentIcon,
                    selectedAgentIconKey = icon?.let { Any() } ?: state.UNSET.selectedAgentIconKey,
                    selectedAgentRoleName = identity?.role?.displayName ?: state.UNSET.selectedAgentRoleName,
                    selectedAgentRoleIcon = selectedAgentRoleIcon ?: state.UNSET.selectedAgentRoleIcon,
                    selectedAgentRoleIconKey = selectedAgentRoleIcon?.let { Any() } ?: state.UNSET.selectedAgentRoleIconKey
                )
            }
        }

        private fun newPlayerAgentSelectionState(
            selectionState: CharacterSelectionState
        ) {
            this.playerAgentSelectionState = selectionState
            mutateState("newPlayerAgentSelectionState") { state ->
                state.copy(
                    isLockedIn = selectionState.isLocked
                )
            }
        }

        override fun onAbandoned() {
            super.onAbandoned()
            check(!remembered)
            check(!abandoned)
            check(!forgotten)
            abandoned = true
        }

        override fun onForgotten() {
            super.onForgotten()
            check(remembered)
            check(!abandoned)
            check(!forgotten)
            forgotten = true
            coroutineScope.cancel()
            _assetLoader?.dispose()
            _mmrClient?.dispose()
        }

        override fun onRemembered() {
            super.onRemembered()
            check(!remembered)
            check(!forgotten)
            check(!abandoned)
            remembered = true
            _coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            _assetLoader = assetsService.createLoaderClient()
            _mmrClient = mmrService.createUserClient(user)
        }

        @SnapshotRead
        private fun stateValueOrUnset(): AgentSelectionPlayerCardState {
            return _state.value ?: AgentSelectionPlayerCardState.UNSET
        }

        @MainThread
        private fun mutateState(
            actionName: String,
            mutate: (state: AgentSelectionPlayerCardState) -> AgentSelectionPlayerCardState
        ) {
            check(inMainLooper())
            val current = stateValueOrUnset()
            val new = mutate(current)
            _state.value = new
            Log.d("AgentSelectionPlayerCardPresenterKt", "mutateState($actionName), current=$current ; new=$new")
        }

        private fun newPlayerIdSideEffect() {
            val playerId = this.playerId!!

            val nameResultError = mutableStateOf<AgentSelectionPlayerCardErrorMessage?>(null)
            val mmrResultError = mutableStateOf<AgentSelectionPlayerCardErrorMessage?>(null)
            val errors = derivedStateOf { listOfNotNull(nameResultError.value, mmrResultError.value) }

            fun errorCount() = errors.value.size

            mutateState("newPlayerIdSideEffect") { state ->
                state.copy(
                    errorCount = 0,
                    getErrors = errors::value
                )
            }

            coroutineScope.launch(playerIdSupervisor!!) {
                var taskContinuation: CompletableJob? = null
                loop {
                    ensureActive()
                    val currentLoopTaskContinuation = taskContinuation
                    val fetch = run {
                        val def = nameService.getPlayerNameAsync(
                            GetPlayerNameRequest(shard = null, signedInUserPUUID = user, listOf(playerId))
                        )
                        runCatching { def.await() }.onFailure { def.cancel() }.getOrThrow()
                    }
                    val nameResult = fetch[playerId]
                    if (
                        fetch.ex != null ||
                        nameResult?.isSuccess != true
                    ) {
                        val cont = Job()
                        var slot = true
                        nameResultError.value = AgentSelectionPlayerCardErrorMessage(
                            component = "GAME NAME",
                            message = when {
                                fetch.ex != null -> {
                                    // TODO: exhaust error type
                                    "UNEXPECTED ERROR OCCURRED WHEN REQUESTING DATA"
                                }
                                nameResult?.isSuccess != true -> {
                                    // TODO: exhaust error type
                                    "UNEXPECTED ERROR OCCURRED WHEN PROCESSING RESPONSE FROM ENDPOINT "
                                }
                                else -> exhaustiveWhenExpressionError()
                            },
                            refresh = refresh@ {
                                checkInMainLooper()
                                if (!slot) return@refresh null
                                slot = false
                                cont.complete()
                                taskContinuation = Job()
                                taskContinuation
                            }
                        )
                        mutateState("newPlayerIdSideEffect_nameResult_fail") { state ->
                            state.copy(errorCount = errorCount())
                        }
                        currentLoopTaskContinuation?.complete()
                        cont.join()
                        LOOP_CONTINUE()
                    }
                    nameResultError.value = null
                    mutateState("newPlayerIdSideEffect_nameResult_success") { state ->
                        val identity = fetch[playerId]!!.getOrThrow()
                        state.copy(
                            playerGameName = identity.gameName,
                            playerGameNameTag = identity.tagLine,
                            errorCount = errorCount()
                        )
                    }
                    currentLoopTaskContinuation?.complete()
                    LOOP_BREAK()
                }
            }
            coroutineScope.launch(playerIdSupervisor!!) {
                var mmrTaskContinuation: CompletableJob? = null
                val mmrResult = strictResultingLoop<SeasonalMMRData>() {
                    ensureActive()
                    val clMmrTaskContinuation = mmrTaskContinuation
                    val fetch = run {
                        val def = mmrClient.fetchSeasonalMMRAsync(ValorantSeasons.ACTIVE_STAGED.act.id, playerId)
                        runCatching { def.await() }.onFailure { def.cancel() }.getOrThrow()
                    }
                    val fetchSuccess = fetch.isSuccess
                    val mmrFetchSuccess = if (fetchSuccess) fetch.getOrThrow().isSuccess else false
                    if (!fetchSuccess || !mmrFetchSuccess) {
                        val cont = Job(coroutineContext.job)
                        var slot = true
                        val refresh = refresh@ {
                            checkInMainLooper()
                            if (!slot) return@refresh null
                            slot = false
                            cont.complete()
                            mmrTaskContinuation = Job()
                            mmrTaskContinuation
                        }
                        when {
                            !fetchSuccess ->  {
                                mmrResultError.value = AgentSelectionPlayerCardErrorMessage(
                                    component = "RANK ICON",
                                    message = "UNEXPECTED ERROR OCCURRED WHEN REQUESTING DATA (${fetch.getErrorCodeOrNull()})",
                                    refresh = refresh
                                )
                                mutateState("newPlayerIdSideEffect_mmrResult_fail") { state ->
                                    state.copy(errorCount = errorCount())
                                }
                            }
                            !mmrFetchSuccess -> run mmrFetchFail@ {
                                fetch
                                    .getOrThrow()
                                    .onRateLimited { info ->
                                        // TODO: check for server TS then compare with NTP
                                        val initialStamp = info.deviceClockUptimeMillis ?: SystemClock.elapsedRealtime()
                                        var stamp = SystemClock.elapsedRealtime()
                                        val retryAfterMs = (info.retryAfter ?: 1.minutes).inWholeMilliseconds
                                        var initialLoop = true
                                        runCatching {
                                            withContext(cont) {
                                                loop {
                                                    ensureActive()
                                                    val retryIn = ((retryAfterMs - (stamp - initialStamp)) / 1000)
                                                        .coerceAtLeast(0)
                                                    mmrResultError.value = AgentSelectionPlayerCardErrorMessage(
                                                        component = "RANK ICON",
                                                        message = "RATE LIMITED, TRY AGAIN IN $retryIn SECOND.",
                                                        refresh = refresh.takeIf { retryIn <= 0 }
                                                    )
                                                    if (initialLoop) {
                                                        mutateState("newPlayerIdSideEffect_mmrResult_fail") { state ->
                                                            state.copy(errorCount = errorCount())
                                                        }
                                                        initialLoop = false
                                                    }
                                                    if (retryIn == 0L) {
                                                        LOOP_BREAK()
                                                    }
                                                    delay(1000 - (retryIn % 1000))
                                                    stamp = SystemClock.elapsedRealtime()
                                                }
                                            }
                                            return@mmrFetchFail
                                        }.onFailure { ex ->
                                            if (!cont.isCompleted || cont.isCancelled) throw ex
                                        }
                                    }
                                mmrResultError.value = AgentSelectionPlayerCardErrorMessage(
                                    component = "RANK ICON",
                                    message = "UNEXPECTED ERROR OCCURRED WHEN PROCESSING RESPONSE FROM ENDPOINT",
                                    refresh = refresh
                                )
                            }
                            else -> exhaustiveWhenExpressionError()
                        }
                        clMmrTaskContinuation?.complete()
                        cont.join()
                        LOOP_CONTINUE()
                    }
                    mmrResultError.value = null
                    mutateState("newPlayerIdSideEffect_mmrResult_success") { state ->
                        state.copy(errorCount = errorCount())
                    }
                    LOOP_BREAK(fetch.getOrThrow().getOrElse { error("") })
                }
                // packaged into APK, even if it fails we just let it crash
                val iconResult = run {
                    assetLoader.loadMemoryCachedCompetitiveRankIcon(mmrResult.competitiveRank)
                        ?.let { return@run Result.success(it) }
                    val def = assetLoader.loadCompetitiveRankIconAsync(mmrResult.competitiveRank)
                    runCatching { def.await() }.onFailure { def.cancel() }.getOrThrow()
                }.getOrElse {
                    mutateState("newPlayerIdSideEffect_iconResult_fail") { state ->
                        state.copy(
                            competitiveTierIcon = state.UNSET.competitiveTierIcon,
                            competitiveTierIconKey = state.UNSET.competitiveTierIconKey
                        )
                    }
                    return@launch
                }
                mutateState("newPlayerIdSideEffect_iconResult") { state ->
                    state.copy(
                        competitiveTierIcon = iconResult,
                        competitiveTierIconKey = Any()
                    )
                }
            }
        }
    }
}