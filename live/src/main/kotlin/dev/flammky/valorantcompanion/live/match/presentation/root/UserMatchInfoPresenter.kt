package dev.flammky.valorantcompanion.live.match.presentation.root

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.base.compose.SnapshotRead
import dev.flammky.valorantcompanion.base.di.koin.getFromKoin
import dev.flammky.valorantcompanion.pvp.map.ValorantMapIdentity
import dev.flammky.valorantcompanion.pvp.mode.ValorantGameMode
import dev.flammky.valorantcompanion.pvp.pregame.PreGameMatchData
import dev.flammky.valorantcompanion.pvp.pregame.PreGameUserClient
import dev.flammky.valorantcompanion.pvp.pregame.PreGameService
import dev.flammky.valorantcompanion.pvp.pregame.ex.PreGameMatchNotFoundException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
class UserMatchInfoPresenter(
    private val pregameService: PreGameService,
    private val authRepository: RiotAuthRepository,
) {

    @Composable
    fun present(): UserMatchInfoUIState {
        val activeAccountState = remember(this) {
            mutableStateOf<AuthenticatedAccount?>(null)
        }
        val initialized = remember(this) {
            mutableStateOf(false)
        }
        DisposableEffect(
            this,
        ) {
            val listener = ActiveAccountListener { old, new ->
                activeAccountState.value = new
            }
            authRepository.registerActiveAccountChangeListener(
                listener
            )
            // listener should already be invoked
            initialized.value = true
            onDispose {
                authRepository.unRegisterActiveAccountListener(listener)
            }
        }
        if (!initialized.value) {
            return UserMatchInfoUIState.UNSET.copy(
                showLoading = true,
                showLoadingOnly = true
            )
        }
        return present(puuid = activeAccountState.value?.model?.id ?: "")
    }

    @Composable
    private fun present(
        puuid: String
    ): UserMatchInfoUIState {
        if (puuid.isBlank()) {
            return UserMatchInfoUIState.UNSET
        }
        return remember(this, puuid) { UserMatchInfoUIStateProducer(puuid) }
            .apply { onRemembered() }
            .produceState()
    }


    private inner class UserMatchInfoUIStateProducer(
        private val puuid: String
    ) : RememberObserver {
        private val state = mutableStateOf(UserMatchInfoUIState.UNSET)
        private var rememberedByComposition = false
        private var forgottenByComposition = false
        private var init = true
        // lateinit
        private var _coroutineScope: CoroutineScope? = null
        // lateinit
        private var _preGameUserClient: PreGameUserClient? = null

        private var lastDataRefreshStamp: Long = -1L
        private var lastPingRefreshStamp: Long = -1L

        private val coroutineScope: CoroutineScope
            get() = _coroutineScope
                ?: error("coroutineScope is not initialized")

        private val preGameUserClient: PreGameUserClient
            get() = _preGameUserClient
                ?: error("preGameUserClient is not initialized")

        private val gamePodsPings = mutableMapOf<String, Int>()

        private var refreshErrorMessage: String? = null

        private var autoRefreshScheduler: Job? = null
        private var currentInitialRefresh: Job? = null
        private var currentAutoInitiatedRefresh: Job? = null
        private var currentUserInitiatedRefresh: Job? = null

        private var pingPollHandle by mutableStateOf<Any>(Any())
        private var pingPollScheduler: Job? = null
        private var currentUpdatePing: Job? = null

        private var stateGamePodID: String? = null

        @SnapshotRead
        fun produceState(): UserMatchInfoUIState {
            check(rememberedByComposition) {
                "produceState is called before onRemembered"
            }
            check(!forgottenByComposition) {
                "produceState is called after onForgotten"
            }
            if (init) {
                initialize()
            }
            return state.value
        }

        override fun onAbandoned() {
            dispose()
        }

        override fun onForgotten() {
            dispose()
        }

        override fun onRemembered() {
            if (rememberedByComposition) return
            rememberedByComposition = true
            _coroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
        }

        private fun dispose() {
            _coroutineScope?.cancel()
            _preGameUserClient?.dispose()
        }

        private fun initialize() {
            check(init)
            init = false
            _preGameUserClient = pregameService.createUserClient(puuid)
            mutateState("initialize") {
                UserMatchInfoUIState.UNSET
            }
            initialRefresh()
            coroutineScope.launch {
                currentInitialRefresh!!.join()
                // on by default
                initializeAutoRefresh()
                initialEventSink()
            }
        }

        private fun initialRefresh() {
            mutateState("initialRefresh") { state ->
                state.copy(showLoading = true, showLoadingOnly = true)
            }
            currentInitialRefresh = coroutineScope.launch {
                refresh("initialize")
                mutateState("initialRefresh_done") { state ->
                    state.copy(showLoading = false, showLoadingOnly = false)
                }
            }
        }

        private fun userInitiatedRefresh() {
            check(currentInitialRefresh?.isActive == false)

            if (currentUserInitiatedRefresh?.isActive == true) return
            if (currentAutoInitiatedRefresh?.isActive == true) currentAutoInitiatedRefresh?.cancel()

            mutateState("userRefresh") { state ->
                state.copy(showLoading = true, showLoadingOnly = false)
            }

            currentUserInitiatedRefresh = coroutineScope.launch {
                refresh("userInitiatedRefresh")
                mutateState("userRefresh_done") { state ->
                    state.copy(showLoading = false, showLoadingOnly = false)
                }
            }
        }

        private fun initialEventSink() =
            mutateState("prepareInitialEventSink") { state ->
                state.copy(
                    setAutoRefresh = ::setAutoRefresh,
                    userRefresh = ::userInitiatedRefresh,
                )
            }

        private fun initializeAutoRefresh() {
            check(autoRefreshScheduler?.isActive != true)
            autoRefreshScheduler = autoRefreshScheduler()
            mutateState("initializeAutoRefresh") { state ->
                state.copy(autoRefreshOn = autoRefreshScheduler?.isActive == true)
            }
        }

        private fun setAutoRefresh(on: Boolean) {
            // TODO
        }

        private fun autoRefreshScheduler(): Job {
            return coroutineScope.launch {
                currentInitialRefresh?.join()
                while (true) {
                    if (lastDataRefreshStamp != -1L) {
                        val elapsed = SystemClock.elapsedRealtime() - lastDataRefreshStamp
                        delay(1000 - elapsed)
                    }
                    if (currentUserInitiatedRefresh?.isActive == true) {
                        runCatching { currentUserInitiatedRefresh?.join() }
                        continue
                    }
                    currentAutoInitiatedRefresh = launch { refresh("autoRefreshScheduler") }
                    runCatching { currentAutoInitiatedRefresh?.join() }
                }
            }
        }

        private suspend fun refresh(
            actionName: String
        ) {
            refreshSideEffect()

            var preGameFetchEx: Exception? = null
            val preGame = run {
                preGameUserClient
                    .hasPreGameMatchData()
                    .await()
                    .onSuccess { hasData ->
                        if (!hasData) return@run null
                    }
                    .onFailure { ex ->
                        ex as Exception
                        preGameFetchEx = ex
                        return@run null
                    }

                preGameUserClient
                    .fetchCurrentPreGameMatchData()
                    .await()
                    .getOrElse { ex ->
                        ex as Exception
                        if (ex !is PreGameMatchNotFoundException) {
                            preGameFetchEx = ex
                        }
                        null
                    }
            }

            if (preGame != null) {
                newPreGameData(preGame)
                pollUpdatePing()
                return
            }

            // Although preGame fetch might've throw exception we still fetch for inGame either way

            var inGameFetchEx: Exception? = null
            val inGame = run {
                null
            }

            if (inGame != null) {
                // newInGameData(inGame)
            }

            if (preGameFetchEx != null) {
                preGameFetchFailure(preGameFetchEx as Exception)
                cancelUpdatePing()
                return
            }

            if (inGameFetchEx != null) {
                // inGameFetchFailure(inGameFetchEx as Exception)
            }

            notInAnyMatch()
            cancelUpdatePing()
        }

        private fun newPreGameData(
            data: PreGameMatchData
        ) {
            mutateState("newPreGameData") { state ->
                state.copy(
                    inPreGame = true,
                    // we can assume that we are not in any ongoing match
                    inGame = false,
                    mapId = data.mapId,
                    mapName = ValorantMapIdentity.ofID(data.mapId)?.display_name
                        ?: "UNKNOWN_MAP_NAME",
                    gameModeName = ValorantGameMode.fromQueueID(data.queueId)?.displayName
                        ?: run {
                            if (data.provisioningFlow.lowercase() == "CustomGame".lowercase()) {
                                "Custom Game"
                            } else {
                                "UNKNOWN_GAME_MODE"
                            }
                        },
                    gamePodName = parseGamePodName(data.gamePodId),
                    gamePodPingMs = run {
                        stateGamePodID = data.gamePodId
                        gamePodsPings[data.gamePodId] ?: -1
                    },
                    errorMessage = "",
                )
            }
        }

        private fun notInAnyMatch() {
            this.refreshErrorMessage = null
            mutateState("notInAnyMatch") { state ->
                state.copy(
                    inPreGame = false,
                    inGame = false,
                    mapId = "",
                    mapName = "",
                    gameModeName = "",
                    gamePodName = "",
                    gamePodPingMs = run {
                        stateGamePodID = null
                        -1
                    },
                    errorMessage = currentErrorMessage(),
                    needManualRefresh = needManualRefresh(),
                )
            }
        }

        private fun preGameFetchFailure(
            cause: Exception,
            // errorCode: Int
        ) {
            this.refreshErrorMessage = "unexpected error occurred" // + " ($errorCode)"
            mutateState("preGameFetchFailure") { state ->
                state.copy(
                    inPreGame = false,
                    inGame = false,
                    mapId = "",
                    mapName = "",
                    gameModeName = "",
                    gamePodName = "",
                    gamePodPingMs = -1,
                    errorMessage = currentErrorMessage(),
                    needManualRefresh = needManualRefresh()
                )
            }
        }

        private fun pollUpdatePing() {
            pingPollHandle = Any()
            if (pingPollScheduler?.isActive != true) {
                pingPollScheduler = pingPollScheduler()
            }
        }

        private fun cancelUpdatePing() {
            if (pingPollHandle is CancellationException) {
                check(currentUpdatePing?.isActive != true)
                return
            }
            pingPollHandle = CancellationException()
            currentUpdatePing?.cancel()
        }

        private fun pingPollScheduler(): Job {
            return coroutineScope.launch {
                var polling: Any? = null
                var polled: Any? = null
                while (true) {
                    snapshotFlow { pingPollHandle }.first { it != polled }
                    polling = pingPollHandle
                    if (polling != CancellationException()) {
                        currentUpdatePing = launch {
                            if (lastPingRefreshStamp != -1L) {
                                val elapsed = SystemClock.elapsedRealtime() - lastPingRefreshStamp
                                delay(1000 - elapsed)
                            }
                            lastPingRefreshStamp = SystemClock.elapsedRealtime()
                            preGameUserClient
                                .fetchPingMillis()
                                .await()
                                .onSuccess { pings -> onNewPingData(pings) }
                                .onFailure { gamePodsPings.clear() }
                        }.also {
                            runCatching { it.join() }
                        }
                    }
                    polled = polling
                }
            }
        }

        private fun onNewPingData(
            data: Map<String, Int>
        ) {
            gamePodsPings.clear()
            data.forEach { ping -> gamePodsPings[ping.key] = ping.value }
            mutateState("onNewPingData") { state ->
                state.copy(gamePodPingMs = gamePodsPings[stateGamePodID] ?: -1)
            }
        }

        // TODO: map id instead of guessing
        private fun parseGamePodName(id: String): String {
            val hasPodNum = id.last().isDigit()
            val segTake = if (hasPodNum) 2 else 1
            var seg = 1
            return id.takeLastWhile { char ->
                if (char == '-') seg++
                seg <= segTake
            }.ifBlank {
                "???"
            }
        }

        private fun refreshSideEffect() {
            lastDataRefreshStamp = SystemClock.elapsedRealtime()
        }

        private fun currentErrorMessage(): String? {
            return this.refreshErrorMessage
        }

        private fun needManualRefresh(): Boolean {
            return currentErrorMessage() != null
        }

        private fun mutateState(
            actionName: String,
            mutator: (UserMatchInfoUIState) -> UserMatchInfoUIState
        ) {
            Log.d("UserMatchInfoPresenter.kt", "UserMatchInfoUIStateProducer.mutateState($actionName)")
            state.value = mutator(state.value)
        }
    }
}



@Composable
fun rememberUserMatchInfoPresenter(
    pregameService: PreGameService = getFromKoin(),
    authRepository: RiotAuthRepository = getFromKoin()
): UserMatchInfoPresenter {
    return remember(pregameService, authRepository) {
        UserMatchInfoPresenter(
            pregameService = pregameService,
            authRepository = authRepository
        )
    }
}