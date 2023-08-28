package dev.flammky.valorantcompanion.live.loadout.presentation

import android.os.SystemClock
import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.*
import androidx.compose.ui.util.fastForEachIndexed
import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.base.checkInMainLooper
import dev.flammky.valorantcompanion.base.compose.RememberObserver
import dev.flammky.valorantcompanion.base.di.DependencyInjector
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.requireInject
import dev.flammky.valorantcompanion.base.isUNSET
import dev.flammky.valorantcompanion.base.kt.coroutines.awaitOrCancelOnException
import dev.flammky.valorantcompanion.base.loop
import dev.flammky.valorantcompanion.live.BuildConfig
import dev.flammky.valorantcompanion.pvp.loadout.*
import kotlinx.collections.immutable.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

@Composable
internal fun rememberSprayLoadoutPickerDetailPresenter(
    dependencyInjector: DependencyInjector = LocalDependencyInjector.current
): SprayLoadoutPickerDetailPresenter {
    return rememberSprayLoadoutPickerDetailPresenter(
        loadoutService = dependencyInjector.requireInject(),
        assetService = dependencyInjector.requireInject()
    )
}

@Composable
internal fun rememberSprayLoadoutPickerDetailPresenter(
    loadoutService: PlayerLoadoutService,
    assetService: ValorantAssetsService
): SprayLoadoutPickerDetailPresenter {
    return remember(loadoutService) {
        SprayLoadoutPickerDetailPresenter(loadoutService, assetService)
    }
}

@Composable
internal fun SprayLoadoutPickerDetailPresenter.present(
    equipIdsKey: Any,
    equipIds: ImmutableList<String>,
    selectedSlotIndex: Int,
    authRepository: RiotAuthRepository = LocalDependencyInjector.current.requireInject()
): SprayLoadoutPickerDetailScreenState {
    val activeAccountState = remember(this, authRepository) {
        mutableStateOf<AuthenticatedAccount?>(null)
    }
    val loaded = remember(activeAccountState) {
        mutableStateOf<Boolean>(false)
    }
    DisposableEffect(
        activeAccountState
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
    return activeAccountState.value
        ?.let { auth ->
            present(
                user = auth.model.id,
                equipIdsKey = equipIdsKey,
                equipIds = equipIds,
                selectedSlotIndex = selectedSlotIndex
            )
        }
        ?: SprayLoadoutPickerDetailScreenState.UNSET
}

internal class SprayLoadoutPickerDetailPresenter(
    private val loadoutService: PlayerLoadoutService,
    private val assetService: ValorantAssetsService
) {

    // TODO: possibility of shared-transition where accept an initial data

    @Composable
    fun present(
        user: String,
        equipIdsKey: Any,
        equipIds: ImmutableList<String>,
        selectedSlotIndex: Int,
    ): SprayLoadoutPickerDetailScreenState {
        return remember(this, user) {
            StateProducer(user)
        }.apply {
            SideEffect {
                produce(equipIdsKey, equipIds, selectedSlotIndex)
            }
        }.readSnapshot()
    }

    @Composable
    fun present(
        user: String,
        equipIdsKey: Any,
        equipIds: ImmutableList<String>,
        selectedEquipSlotId: String,
    ): SprayLoadoutPickerDetailScreenState = present(
        user = user,
        equipIdsKey = equipIdsKey,
        equipIds = equipIds,
        selectedSlotIndex = remember(equipIdsKey, selectedEquipSlotId) {
            equipIds.indexOf(selectedEquipSlotId)
        }
    )


    private inner class StateProducer(
        private val user: String
    ) : RememberObserver {

        private val _state = mutableStateOf<SprayLoadoutPickerDetailScreenState?>(null)
        private var remembered = false
        private var forgotten = false
        private var abandoned = false
        private var initialProduce = true
        private val lifetime = SupervisorJob()
        private var _coroutineScope: CoroutineScope? = null
        private var _loadoutClient: PlayerLoadoutClient? = null
        private var _assetClient: ValorantAssetsLoaderClient? = null

        private var producer: Job? = null

        private val producing
            get() = producer?.isActive == true

        private val coroutineScope
            get() = _coroutineScope!!

        private val loadoutClient
            get() = _loadoutClient!!

        private val assetClient
            get() = _assetClient!!

        private var _slotEquipIdsKey: Any = Any()
        private var _slotEquipIds: ImmutableList<String> = persistentListOf()
        private val _slotTotal: Int get() = _slotEquipIds.size
        private var _selectedSlotEquipIndex = -1
        private var _latestData: PlayerLoadout? = null
        private var _selectedReplacementSprayUUID: String? = null

        private val sprayDisplayNameCache = mutableMapOf<String, String>()

        private var dataPollTS = -1L

        private var latestReplaceSprayJob: Job? = null
        private var latestReplaceSprayUUID: String? = null

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
            assetClient.dispose()
        }

        override fun onRemembered() {
            super.onRemembered()
            check(!remembered)
            check(!forgotten)
            check(!abandoned)
            remembered = true
            _coroutineScope = CoroutineScope(Dispatchers.Main + lifetime)
            _loadoutClient = loadoutService.createClient()
            _assetClient = assetService.createLoaderClient()
        }

        @MainThread
        fun produce(
            equipSlotIdsKey: Any,
            equipSlotIds: ImmutableList<String>,
            selectedSlotIndex: Int
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
            produceParams(
                equipSlotIdsKey,
                equipSlotIds,
                selectedSlotIndex
            )
        }

        fun readSnapshot() = stateValueOrUnset()

        @MainThread
        private fun produceParams(
            slotEquipIdsKey: Any,
            slotEquipIds: ImmutableList<String>,
            selectedSlotEquipIndex: Int
        ) {
            if (initialProduce) {
                initialProduce = false
                onInitialProduce()
            }

            var slotEquipChanged = false
            var slotEquipIndexChanged = false

            if (slotEquipIdsKey != _slotEquipIdsKey) {
                _slotEquipIdsKey = slotEquipIdsKey
                _slotEquipIds = slotEquipIds
                slotEquipChanged = true
            }

            if (selectedSlotEquipIndex != _selectedSlotEquipIndex) {
                _selectedSlotEquipIndex = selectedSlotEquipIndex
                slotEquipIndexChanged = true
            }

            if (slotEquipChanged || slotEquipIndexChanged) mutateState("produceParams_slot_changed") { state ->
                val latestData = _latestData
                val slotContents = latestData
                    ?.let { data -> slotContents(slotEquipIds, data) }
                    ?: persistentMapOf()
                val selectedSlotSprayDisplayName = slotContents[slotEquipIds[selectedSlotEquipIndex]]
                    ?.let { sprayDisplayNameCache[it.sprayId] }
                state.copy(
                    slotEquipIdsKey = slotEquipIdsKey,
                    slotEquipIds = slotEquipIds,
                    selectedSlotEquipIndex = selectedSlotEquipIndex,
                    slotContentsKey = if (slotEquipChanged) Any() else state.slotContentsKey,
                    slotContents = if (slotEquipChanged) {
                        _latestData?.let { data -> slotContents(slotEquipIds, data) }
                            ?: persistentMapOf()
                    } else state.slotContents,
                    hasSelectedSlotSprayDisplayName = selectedSlotSprayDisplayName != null,
                    selectedSlotSprayDisplayName = selectedSlotSprayDisplayName ?: "",
                    selectedReplacementSprayUUID = null,
                    hasSelectedReplacementSprayDisplayName = false,
                    selectedReplacementSprayDisplayName = ""
                )
            }
        }

        private fun onInitialProduce() {
            check(!producing)
            mutateState("onInitialProduce") { state ->
                state.copy(
                    selectSpray = ::selectSpray,
                    confirmReplaceSpray = ::confirmReplaceSpray
                )
            }
            producer = produceState()
        }

        private fun produceState(): Job {
            return coroutineScope.launch {
                loop {
                    if (dataPollTS > -1) {
                        delay(1000 - (SystemClock.elapsedRealtime() - dataPollTS))
                    }
                    dataPollTS = SystemClock.elapsedRealtime()
                    fetchData()
                }
            }
        }

        private suspend fun fetchData() {
            coroutineContext.ensureActive()
            val def = loadoutClient.fetchPlayerLoadoutAsync(user)
            val result = def.awaitOrCancelOnException()
            coroutineContext.ensureActive()
            result
                .onFailure { ex ->
                    onFetchFailure(ex as Exception)
                }.onSuccess { data ->
                    onFetchSuccess(data)
                }
        }

        private fun onFetchSuccess(
            data: PlayerLoadout
        ) {
            onNewData("onFetchSuccess", data)
        }

        private fun onFetchFailure(
            ex: Exception,
            // TODO: error code: Int
        ) {
            this._latestData = null
            mutateState("onFetchFailure") { state ->
                state.copy(
                    slotContentsKey = Any(),
                    slotContents = persistentMapOf(),
                    hasSelectedSlotSprayDisplayName = false,
                    selectedSlotSprayDisplayName = "",
                    canReplaceSpray = false
                )
            }
        }

        private fun confirmReplaceSprayResponse(
            data: PlayerLoadout
        ) {
            _selectedReplacementSprayUUID = null
            mutateState("confirmReplaceSprayResponse_resetReplacementField") { state ->
                state.copy(
                    selectedReplacementSprayUUID = null,
                    hasSelectedReplacementSprayDisplayName = false,
                    selectedReplacementSprayDisplayName = ""
                )
            }
            onNewData("confirmReplaceSprayResponse_updateFromRemoteInfo", data)
        }

        private fun onNewData(
            actionName: String,
            data: PlayerLoadout
        ) {
            val currentData = _latestData
            this._latestData = data

            mutateState(actionName) { state ->
                var stage = state

                if (currentData?.version != data.version) {
                    val equipID = _slotEquipIds.getOrNull(_selectedSlotEquipIndex)
                    val selectedSpray = equipID?.let {
                        data.sprays.find { it.equipSlotId == equipID }
                    }
                    val selectedSlotSprayDisplayName = selectedSpray?.let { item ->
                        sprayDisplayNameCache[item.sprayId]
                    }
                    if (selectedSlotSprayDisplayName == null) {
                        fetchSelectedSprayDisplayName()
                    }
                    stage = stage.copy(
                        slotContentsKey = Any(),
                        slotContents = slotContents(_slotEquipIds, data),
                        hasSelectedSlotSprayDisplayName = selectedSlotSprayDisplayName != null,
                        selectedSlotSprayDisplayName = selectedSlotSprayDisplayName ?: "",
                        canReplaceSpray = canReplaceSpray(selectedSpray?.sprayId, state.selectedReplacementSprayUUID),
                    )

                    // TODO: verify that the slot is present in the info
                }

                stage
            }
        }

        // TODO: staging
        private fun mutateState(
            action: String,
            mutate: (SprayLoadoutPickerDetailScreenState) -> SprayLoadoutPickerDetailScreenState
        ) {
            checkInMainLooper()
            val current = stateValueOrUnset()
            val new = mutate(current)
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "live.loadout.presentation.SprayLoadoutPickerDetailPresenterKt: StateProducer_mutateState($action), result=$new"
            )
            _state.value = new
        }

        private fun canReplaceSpray(
            selected: String?,
            selectedReplacement: String?
        ): Boolean {
            return selected != null &&
                    selectedReplacement != null &&
                    selected != selectedReplacement &&
                    latestReplaceSprayJob?.isActive != true
        }

        private fun slotContents(
            equipIds: List<String>,
            data: PlayerLoadout
        ): ImmutableMap<String, SprayLoadoutItem> {
            val builder = persistentMapOf<String, SprayLoadoutItem>().builder()

            equipIds.forEach { id ->
                data.sprays.find { item -> item.equipSlotId == id }?.let { builder[id] = it }
            }

            return builder.build()
        }

        private fun explicitLoadingWithMessage(): Pair<Boolean, String?> {

            if (latestReplaceSprayJob?.isActive == true) {
                return true to "REPLACING SPRAY ..."
            }

            return false to null
        }

        private fun selectSpray(selected: String) {
            this._selectedReplacementSprayUUID = selected
            mutateState("selectSpray") { state ->
                var staged = state

                if (staged.selectedReplacementSprayUUID != selected) {
                    val displayName = sprayDisplayNameCache[selected]
                    staged = staged.copy(
                        selectedReplacementSprayUUID = selected,
                        hasSelectedReplacementSprayDisplayName = displayName != null,
                        selectedReplacementSprayDisplayName = displayName ?: "",
                        canReplaceSpray = run {
                            val equipId = staged.slotEquipIds.getOrNull(staged.selectedSlotEquipIndex)
                            val current = staged.slotContents[equipId]
                            canReplaceSpray(current?.sprayId, selected)
                        }
                    )
                    if (displayName == null) {
                        fetchSelectedReplacementSprayDisplayName()
                    }
                }

                staged
            }
        }


        private fun confirmReplaceSpray() {
            val state = stateValueOrUnset().also { if (it.isUNSET) return }
            val selected = this._selectedReplacementSprayUUID
            val data = this._latestData
            val equipSlotId = state.run { slotEquipIds.getOrNull(selectedSlotEquipIndex) }
            if (
                selected == null || data == null || equipSlotId == null ||
                selected == latestReplaceSprayUUID &&
                latestReplaceSprayJob?.isActive == true
            ) {
                return
            }
            latestReplaceSprayUUID = selected
            latestReplaceSprayJob?.cancel()
            latestReplaceSprayJob = coroutineScope.launch {
                val changeData = PlayerLoadoutChangeData(
                    guns = data.guns,
                    sprays = data.sprays.toPersistentList().mutate { mut ->
                        var index = -1

                        run {
                            mut.fastForEachIndexed { i, sprayLoadoutItem ->
                                if (sprayLoadoutItem.equipSlotId == equipSlotId) {
                                    index = i
                                    return@run
                                }
                            }
                        }

                        if (index < 0) return@launch

                        mut.removeAt(index)
                        mut.add(
                            SprayLoadoutItem(
                                equipSlotId,
                                selected
                            )
                        )
                    },
                    identity = data.identity,
                    incognito = data.incognito
                )
                loadoutClient
                    .modifyPlayerLoadoutAsync(user, changeData)
                    .awaitOrCancelOnException()
                    .fold(
                        onSuccess = {
                            confirmReplaceSprayResponse(it)
                        },
                        onFailure = {
                            // TODO: ask to retry
                        }
                    )
            }.apply {
                invokeOnCompletion { ex ->
                    if (ex == null) mutateState("confirmReplaceSpray_completion") { state ->
                        val (explicitLoading, explicitLoadingMessage) = explicitLoadingWithMessage()
                        state.copy(
                            canReplaceSpray = canReplaceSpray(state.slotContents[state.slotEquipIds[state.selectedSlotEquipIndex]]?.sprayId, latestReplaceSprayUUID),
                            explicitLoading = explicitLoading,
                            explicitLoadingMessage = explicitLoadingMessage
                        )
                    }
                }
            }
            mutateState("confirmReplaceSpray") { mut ->
                val (explicitLoading, explicitLoadingMessage) = explicitLoadingWithMessage()
                mut.copy(
                    canReplaceSpray = canReplaceSpray(selected, latestReplaceSprayUUID),
                    explicitLoading = explicitLoading,
                    explicitLoadingMessage = explicitLoadingMessage
                )
            }
        }

        private var latestFetchSelectedReplacementSprayDisplayNameJob: Job? = null
        private var latestFetchSelectedReplacementSprayDisplayNameUUID: String? = null
        private fun fetchSelectedReplacementSprayDisplayName() {
            val uuid = _selectedReplacementSprayUUID
            if (
                uuid == null ||
                uuid == latestFetchSelectedReplacementSprayDisplayNameUUID &&
                latestFetchSelectedReplacementSprayDisplayNameJob?.isActive == true
            ) {
                return
            }
            latestFetchSelectedReplacementSprayDisplayNameUUID = uuid
            latestFetchSelectedReplacementSprayDisplayNameJob?.cancel()
            latestFetchSelectedReplacementSprayDisplayNameJob = coroutineScope.launch {
                assetClient
                    .loadSprayIdentityAsync(uuid)
                    .awaitOrCancelOnException()
                    .fold(
                        onSuccess = { identity ->
                            sprayDisplayNameCache[uuid] = identity.displayName
                            mutateState("fetchSelectedReplacementSprayDisplayName_success") { state ->
                                check(state.selectedReplacementSprayUUID == uuid)
                                state.copy(
                                    hasSelectedReplacementSprayDisplayName = true,
                                    selectedReplacementSprayDisplayName = identity.displayName
                                )
                            }
                        },
                        onFailure = {
                            // TODO: ask to retry
                        }
                    )
            }
        }

        private var latestFetchSelectedSprayDisplayNameJob: Job? = null
        private var latestFetchSelectedSprayDisplayNameUUID: String? = null
        private fun fetchSelectedSprayDisplayName() {
            val uuid = run {
                val sprays = _latestData?.sprays
                    ?: return@run null
                val equipSlot = _slotEquipIds.getOrNull(_selectedSlotEquipIndex)
                sprays.find { it.equipSlotId == equipSlot }?.sprayId
            }
            if (
                uuid == null ||
                uuid == latestFetchSelectedSprayDisplayNameUUID &&
                latestFetchSelectedSprayDisplayNameJob?.isActive == true
            ) {
                return
            }
            latestFetchSelectedSprayDisplayNameUUID = uuid
            latestFetchSelectedSprayDisplayNameJob?.cancel()
            latestFetchSelectedSprayDisplayNameJob = coroutineScope.launch {
                assetClient
                    .loadSprayIdentityAsync(uuid)
                    .awaitOrCancelOnException()
                    .fold(
                        onSuccess = { identity ->
                            sprayDisplayNameCache[uuid] = identity.displayName
                            mutateState("fetchSelectedSprayDisplayName_success") { state ->
                                check(state.slotContents[state.slotEquipIds[state.selectedSlotEquipIndex]]?.sprayId == uuid)
                                state.copy(
                                    hasSelectedSlotSprayDisplayName = true,
                                    selectedSlotSprayDisplayName = identity.displayName
                                )
                            }
                        },
                        onFailure = {
                            // TODO: ask to retry
                        }
                    )
            }
        }

        private fun stateValueOrUnset() = _state.value ?: SprayLoadoutPickerDetailScreenState.UNSET
    }
}