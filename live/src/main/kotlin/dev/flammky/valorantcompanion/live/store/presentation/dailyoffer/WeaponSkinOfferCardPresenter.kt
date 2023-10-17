package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.weapon.skin.WeaponSkinIdentity
import dev.flammky.valorantcompanion.base.ProjectTree
import dev.flammky.valorantcompanion.base.checkInMainLooper
import dev.flammky.valorantcompanion.base.compose.RememberObserver
import dev.flammky.valorantcompanion.base.di.DependencyInjector
import dev.flammky.valorantcompanion.base.di.requireInject
import dev.flammky.valorantcompanion.base.kt.coroutines.awaitOrCancelOnException
import dev.flammky.valorantcompanion.base.loop
import dev.flammky.valorantcompanion.live.BuildConfig
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreClient
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreService
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCurrency
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier
import kotlinx.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

interface WeaponSkinOfferCardPresenter {

    @Composable
    fun present(
        id: String,
        cost: StoreCost
    ): WeaponSkinOfferCardState
}

@Composable
fun rememberWeaponSkinOfferCardPresenter(
    di: DependencyInjector
): WeaponSkinOfferCardPresenter {
    return rememberWeaponSkinOfferCardPresenter(
        storeService = di.requireInject(),
        assetService = di.requireInject()
    )
}

@Composable
fun rememberWeaponSkinOfferCardPresenter(
    storeService: ValorantStoreService,
    assetService: ValorantAssetsService
): WeaponSkinOfferCardPresenter {
    return remember(storeService, assetService) { WeaponSkinOfferCardPresenterImpl(storeService, assetService) }
}

private class WeaponSkinOfferCardPresenterImpl(
    private val storeService: ValorantStoreService,
    private val assetService: ValorantAssetsService
) : WeaponSkinOfferCardPresenter {

    @Composable
    override fun present(id: String, cost: StoreCost): WeaponSkinOfferCardState {
        val producer = remember(this, id) {
            StateProducer(id)
        }.apply {
            SideEffect {
                produceParams(cost)
            }
        }
        return producer.readSnapshot()
    }


    // TODO: change mutateState with stageState grouping instead
    private inner class StateProducer(
        private val id: String
    ) : RememberObserver {

        private val _state = mutableStateOf<WeaponSkinOfferCardState?>(null)

        private var remembered = false
        private var forgotten = false
        private var abandoned = false
        private var initialProduce = true
        private val lifetime = SupervisorJob()
        private var _coroutineScope: CoroutineScope? = null
        private var _storeClient: ValorantStoreClient? = null
        private var _assetClient: ValorantAssetsLoaderClient? = null

        private var producer: Job? = null

        private val producing
            get() = producer?.isActive == true

        private val coroutineScope
            get() = _coroutineScope!!

        private val storeClient
            get() = _storeClient!!

        private val assetClient
            get() = _assetClient!!

        private var _cost: StoreCost? = null

        private var currentLoadCurrencyJob: Job? = null

        private var pendingRefreshContinuations = mutableListOf<Continuation<Unit>>()

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
            assetClient.dispose()
        }

        override fun onRemembered() {
            super.onRemembered()
            check(!remembered)
            check(!forgotten)
            check(!abandoned)
            remembered = true
            _coroutineScope = CoroutineScope(Dispatchers.Main + lifetime)
            _storeClient = storeService.createClient("")
            _assetClient = assetService.createLoaderClient()
        }

        fun produceParams(cost: StoreCost) {
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
                cost
            )
        }

        fun readSnapshot() : WeaponSkinOfferCardState {
            return stateValueOrUnset()
        }

        private fun stateValueOrUnset(): WeaponSkinOfferCardState {
            return _state.value ?: WeaponSkinOfferCardState.UNSET
        }

        private fun invalidateParams(
            cost: StoreCost
        ) {
            invalidateCostParam(cost)
        }

        private fun invalidateCostParam(
            cost: StoreCost
        ) {
            if (cost != this._cost) {
                val currentCost = this._cost
                this._cost = cost
                onNewCostParam(currentCost, cost)
            }
        }

        private fun onNewCostParam(
            old: StoreCost?,
            new: StoreCost?
        ) {
            if (new == null) {
                mutateState("onNewCostParam()") { state ->
                    state.copy(costText = "", costImageKey = Any(), costImage = LocalImage.None)
                }
                return
            }
            val text = costText(new)
            mutateState("onNewCostParam()") { state ->
                if (state.costText == text) return@mutateState state
                state.copy(costText = text)
            }
            if (new.currency != old?.currency) {
                onNewCostCurrencyParam(old?.currency, new.currency)
            }
        }

        private fun onNewCostCurrencyParam(
            old: StoreCurrency?,
            new: StoreCurrency
        ) {
            mutateState("onNewCostCurrencyParam") { state ->
                state.copy(
                    costImageKey = Any(),
                    costImage = LocalImage.None
                )
            }
            loadCurrencyImage(new)
        }

        private fun loadCurrencyImage(
            currency: StoreCurrency
        ) {
            currentLoadCurrencyJob?.cancel()
            currentLoadCurrencyJob = coroutineScope.launch {
                var stamp: Long
                loop {
                    assetClient
                        .loadCurrencyImageAsync(currency.uuid)
                        .awaitOrCancelOnException()
                        .fold(
                            onSuccess = { data ->
                                mutateState("loadCurrencyImage(${currency.uuid})") { state ->
                                    state.copy(
                                        costImageKey = Any(),
                                        costImage = data
                                    )
                                }
                                LOOP_BREAK()
                            },
                            onFailure = { ex ->
                                stamp = SystemClock.elapsedRealtime()
                                suspendCancellableCoroutine<Unit> { cont ->
                                    newPendingRefreshContinuation(cont)
                                }
                                val elapsed = SystemClock.elapsedRealtime() - stamp
                                delay(1000 - elapsed)
                                LOOP_CONTINUE()
                            }
                        )
                }
            }
        }

        private fun costText(
            cost: StoreCost
        ): String {
            val amountStr = cost.amount.toString()
            if (amountStr.isEmpty() || amountStr.length == 1) return amountStr
            return amountStr
                .first()
                .plus(amountStr.drop(1).chunked(3).joinToString(prefix = ",", separator = ","))
        }


        private fun onInitialProduce() {
            mutateState("onInitialProduce") { state ->
                state.UNSET
            }
            producer = produceState()
        }

        private fun produceState(): Job {
            return coroutineScope.launch {
                listOf(
                    produceWeaponSkinIdentity(),
                    produceWeaponSkinImage()
                ).joinAll()
            }
        }

        private fun produceWeaponSkinIdentity(): Job {
            return coroutineScope.launch {
                var stamp = 0L
                loop {
                    assetClient
                        .loadWeaponSkinIdentityAsync(id)
                        .awaitOrCancelOnException()
                        .fold(
                            onSuccess = {
                                onProduceWeaponSkinIdentitySuccess(it)
                                LOOP_BREAK()
                            },
                            onFailure = {
                                stamp = SystemClock.elapsedRealtime()
                                onProduceWeaponSkinIdentityFailure(it as Exception)
                                val elapsed = SystemClock.elapsedRealtime() - stamp
                                delay(1000 - elapsed)
                            }
                        )
                }
            }
        }

        private suspend fun onProduceWeaponSkinIdentitySuccess(
            identity: WeaponSkinIdentity
        ) {
            mutateState("onProduceWeaponSkinIdentitySuccess") { state ->
                state.copy(
                    tier = identity.tier,
                    displayName = identity.displayName
                )
            }
            produceWeaponSkinTierImage(tier = identity.tier)
        }

        private suspend fun produceWeaponSkinTierImage(
            tier: WeaponSkinTier
        ) {
            var stamp = 0L
            loop {
                assetClient.loadWeaponSkinTierImageAsync(tier.uuid).awaitOrCancelOnException()
                    .fold(
                        onSuccess = { data ->
                            mutateState("produceWeaponSkinTierImage_success") { state ->
                                state.copy(
                                    tierImageKey = Any(),
                                    tierImage = data
                                )
                            }
                            LOOP_BREAK()
                        },
                        onFailure = {
                            Log.d("DEBUG", "produceWeaponSkinTierImage_failure=$it")
                            stamp = SystemClock.elapsedRealtime()
                            onProduceWeaponSkinTierFailure(it as Exception)
                            val elapsed = SystemClock.elapsedRealtime() - stamp
                            delay(1000 - elapsed)
                            LOOP_CONTINUE()
                        }
                    )
            }
        }

        private suspend fun onProduceWeaponSkinTierFailure(
            ex: Exception
        ) {
            suspendCancellableCoroutine<Unit> { cont ->
                newPendingRefreshContinuation(cont)
            }
        }

        private suspend fun onProduceWeaponSkinIdentityFailure(
            ex: Exception
        ) {
            suspendCancellableCoroutine<Unit> { cont ->
                newPendingRefreshContinuation(cont)
            }
        }

        private fun produceWeaponSkinImage(): Job {
            return coroutineScope.launch {
                var stamp = 0L
                loop {
                    assetClient
                        .loadWeaponSkinImageAsync(id)
                        .awaitOrCancelOnException()
                        .fold(
                            onSuccess = { data ->
                                mutateState("produceWeaponSkinImage_success") { state ->
                                    state.copy(
                                        displayImageKey = Any(),
                                        displayImage = data
                                    )
                                }
                                // TODO: observe changes
                                LOOP_BREAK()
                            },
                            onFailure = {
                                stamp = SystemClock.elapsedRealtime()
                                suspendCancellableCoroutine<Unit> { cont ->
                                    newPendingRefreshContinuation(cont)
                                }
                                val elapsed = SystemClock.elapsedRealtime() - stamp
                                delay(1000 - elapsed)
                            }
                        )
                }
            }
        }

        private fun mutateState(
            action: String,
            mutate: (WeaponSkinOfferCardState) -> WeaponSkinOfferCardState
        ) {
            checkInMainLooper()
            val current = stateValueOrUnset()
            val new = mutate(current)
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "${ProjectTree.packageName}.WeaponSkinOfferCardPresenterKt: StateProducer_mutateState($action), result=$new"
            )
            if (current === new) return
            _state.value = new
        }

        private fun newPendingRefreshContinuation(
            continuation: Continuation<Unit>
        ) {
            pendingRefreshContinuations.add(continuation)
            if (pendingRefreshContinuations.size == 1) {
                mutateState("newPendingRefreshContinuationInit") { state ->
                    state.copy(requireRefresh = true, refresh = ::performPendingRefreshActions)
                }
            }
        }

        private fun performPendingRefreshActions() {
            if (pendingRefreshContinuations.isEmpty()) {
                // TODO: should not happen, Log
                return
            }
            mutateState("performPendingRefreshActions") { state ->
                state.copy(
                    showLoading = true,
                    requireRefresh = false,
                    refresh = {}
                )
            }
            pendingRefreshContinuations.forEach { cont ->
                cont.resume(Unit)
            }
            pendingRefreshContinuations.clear()
            mutateState("performPendingRefreshActions_dispatched") { state ->
                state.copy(showLoading = false)
            }
        }
    }
}