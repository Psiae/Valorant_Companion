package dev.flammky.valorantcompanion.live.store.presentation.nightmarket

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.*
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
import dev.flammky.valorantcompanion.pvp.store.BonusStore
import dev.flammky.valorantcompanion.pvp.store.ItemType
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCurrency
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier
import kotlinx.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

interface NightMarketOfferCardPresenter {

}

@Composable
fun rememberNightMarketOfferCardPresenter(
    dependencyInjector: DependencyInjector
): NightMarketOfferCardPresenter {
    return remember(dependencyInjector) {
        NightMarketOfferCardPresenterImpl(
            assetsService = dependencyInjector.requireInject()
        )
    }
}

@Composable
fun NightMarketOfferCardPresenter.present(
    itemOffer: BonusStore.ItemOffer
): NightMarketOfferCardState {
    val impl = this as NightMarketOfferCardPresenterImpl
    val reward = itemOffer.rewards.entries.firstOrNull()?.value
        ?: return NightMarketOfferCardState.UNSET
    return impl.present(
        rewardItemID = reward.itemID,
        rewardItemType = reward.itemType,
        discountPercent = itemOffer.discountPercent,
        basePrice = itemOffer.cost,
        discountedPrice = itemOffer.discountedCost
    )
}

private class NightMarketOfferCardPresenterImpl(
    private val assetsService: ValorantAssetsService
) : NightMarketOfferCardPresenter {

    @Composable
    fun present(
        rewardItemID: String,
        rewardItemType: ItemType,
        discountPercent: Int,
        basePrice: StoreCost,
        discountedPrice: StoreCost
    ): NightMarketOfferCardState {
        val producer = producer(
            rewardItemID = rewardItemID,
            rewardItemType = rewardItemType,
            discountPercent = discountPercent,
            basePrice = basePrice,
            discountedPrice = discountedPrice
        )
        return producer.readSnapshot()
    }

    @Composable
    private fun producer(
        rewardItemID: String,
        rewardItemType: ItemType,
        discountPercent: Int,
        basePrice: StoreCost,
        discountedPrice: StoreCost
    ): StateProducer {
        return remember(rewardItemID, rewardItemType) {
            StateProducer(rewardItemID, rewardItemType)
        }.apply {
            SideEffect {
                produceParams(
                    discountPercent,
                    basePrice,
                    discountedPrice
                )
            }
        }
    }

    private inner class StateProducer(
        private val itemID: String,
        private val itemType: ItemType
    ): RememberObserver {

        private val _state = mutableStateOf<NightMarketOfferCardState?>(
            value = null,
            policy = neverEqualPolicy()
        )

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

        private var _discountedPrice: Int? = null
        private var _discountedPriceCurrency: StoreCurrency? = null
        private var _discountPercent: Int? = null
        private var _basePrice: Int? = null

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
            discountPercent: Int,
            basePrice: StoreCost,
            discountedPrice: StoreCost
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
                discountPercent = discountPercent,
                basePrice = basePrice,
                discountedPrice = discountedPrice
            )
        }

        fun readSnapshot(): NightMarketOfferCardState {
            return snapshotOrUnset()
        }

        private fun snapshotOrUnset(): NightMarketOfferCardState {
            return _state.value ?: NightMarketOfferCardState.UNSET
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
                    produceWeaponSkinIdentity(),
                    produceWeaponSkinImage()
                ).joinAll()
            }
        }

        private fun produceWeaponSkinIdentity(): Job {
            return coroutineScope.launch {
                var stamp = 0L
                loop {
                    assetLoader
                        .loadWeaponSkinIdentityAsync(itemID)
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
                assetLoader.loadWeaponSkinTierImageAsync(tier.uuid).awaitOrCancelOnException()
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
                    assetLoader
                        .loadWeaponSkinImageAsync(itemID)
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


        private fun invalidateParams(
            discountPercent: Int,
            basePrice: StoreCost,
            discountedPrice: StoreCost
        ) {
            if (discountPercent != _discountPercent) {
                this._discountPercent = discountPercent
                mutateState("invalidateParams_discountPercentChanged") { state ->
                    state.copy(
                        discountPercentageText = discountPercentageText(discountPercent),
                    )
                }
            }
            if (basePrice.amount.toInt() != _basePrice) {
                this._basePrice = basePrice.amount.toInt()
                mutateState("invalidateParams_basePriceChanged") { state ->
                    state.copy(
                        costText = costText(basePrice.amount.toInt()),
                    )
                }
            }
            if (discountedPrice.amount.toInt() != _discountedPrice) {
                this._discountedPrice = discountedPrice.amount.toInt()
                mutateState("invalidateParams_discountedPriceChanged") { state ->
                    state.copy(
                        discountedAmountText = costText(discountedPrice.amount.toInt()),
                    )
                }
            }
            if (discountedPrice.currency != _discountedPriceCurrency) {
                this._discountedPriceCurrency = discountedPrice.currency
                mutateState("invalidateParams_discountedCurrencyChanged") { state ->
                    state.copy(
                        costImageKey = Any(),
                        costImage = state.UNSET.costImage,
                    )
                }
                newDiscountedPriceCurrency(discountedPrice.currency)
            }
        }

        private var discountedPriceCurrencyImageLoader: Job? = null
        private fun newDiscountedPriceCurrency(
            currency: StoreCurrency
        ) {
            discountedPriceCurrencyImageLoader?.cancel()
            discountedPriceCurrencyImageLoader = coroutineScope.launch {
                assetLoader
                    .loadCurrencyImageAsync(
                        id = currency.uuid,
                    )
                    .awaitOrCancelOnException()
                    .fold(
                        onSuccess = { localImage ->
                            mutateState("loadDiscountedPriceCurrency") { state ->
                                state.copy(
                                    costImageKey = Any(),
                                    costImage = localImage
                                )
                            }
                        },
                        onFailure = {
                            Log.d("DEBUG", "costImage=${it}")
                        }
                    )
            }
        }

        private fun discountPercentageText(
            percentage: Int
        ): String {
            val coerced = percentage.toString().take(3)
            return "-${coerced}%"
        }

        private fun costText(
            cost: Int
        ): String {
            val amountStr = cost.toString()
            if (amountStr.length <= 3) return amountStr
            return amountStr
                .first()
                .plus(amountStr.drop(1).chunked(3).joinToString(prefix = ",", separator = ","))
        }

        private fun mutateState(
            action: String,
            mutate: (NightMarketOfferCardState) -> NightMarketOfferCardState
        ) {
            checkInMainLooper()
            val current = snapshotOrUnset()
            val new = mutate(current)
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "${ProjectTree.packageName}.NightMarketOfferCardPresenterKt: StateProducer_mutateState($action), result=$new"
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