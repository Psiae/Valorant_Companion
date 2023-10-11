package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.bundle.BundleImageType
import dev.flammky.valorantcompanion.assets.bundle.LoadBundleImageRequest
import dev.flammky.valorantcompanion.base.checkInMainLooper
import dev.flammky.valorantcompanion.base.compose.RememberObserver
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead
import dev.flammky.valorantcompanion.base.di.DependencyInjector
import dev.flammky.valorantcompanion.base.di.requireInject
import dev.flammky.valorantcompanion.base.kt.cast
import dev.flammky.valorantcompanion.base.kt.coroutines.awaitOrCancelOnException
import dev.flammky.valorantcompanion.base.loop
import dev.flammky.valorantcompanion.live.BuildConfig
import dev.flammky.valorantcompanion.pvp.store.FeaturedBundleDisplayData
import dev.flammky.valorantcompanion.pvp.store.FeaturedBundleStore
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreClient
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreService
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCurrency
import dev.flammky.valorantcompanion.pvp.store.currency.ofID
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration

interface FeaturedBundleDisplayPresenter {

    @Composable
    fun present(
        offerKey: Any,
        offer: FeaturedBundleStore.Bundle,
        isVisibleToUser: Boolean
    ): FeaturedBundleDisplayState
}

@Composable
fun rememberFeaturedBundleDisplayPresenter(
    di: DependencyInjector
): FeaturedBundleDisplayPresenter {
    return remember(di) {
        FeaturedBundleDisplayPresenterImpl(
            storeService = di.requireInject(),
            assetsService = di.requireInject()
        )
    }
}

private class FeaturedBundleDisplayPresenterImpl(
    private val storeService: ValorantStoreService,
    private val assetsService: ValorantAssetsService
) : FeaturedBundleDisplayPresenter {

    @Composable
    override fun present(
        offerKey: Any,
        offer: FeaturedBundleStore.Bundle,
        isVisibleToUser: Boolean
    ): FeaturedBundleDisplayState {
        val producer = remember(this, offer.id) {
            StateProducer(offer.id)
        }.apply {
            SideEffect {
                produce(isVisibleToUser, offerKey, offer)
            }
        }
        return producer.readSnapshot()
    }

    private inner class StateProducer(
        private val uuid: String
    ) : RememberObserver {

        // TODO: define pipeline ?

        private val _state = mutableStateOf<FeaturedBundleDisplayState?>(null)

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

        private val isVisibleToUser = mutableStateOf(false)
        private var offerKey: Any? = null

        private var isDisplayDataLoaded = false
        private var isImageLoaded = false

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
            _storeClient = storeService.createClient("")
            _assetClient = assetsService.createLoaderClient()
        }

        @MainThread
        fun produce(
            isVisibleToUser: Boolean,
            offerKey: Any,
            offer: FeaturedBundleStore.Bundle
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
                isVisibleToUser,
                offerKey,
                offer
            )
        }

        private fun invalidateParams(
            isVisibleToUser: Boolean,
            offerKey: Any,
            offer: FeaturedBundleStore.Bundle
        ) {
            if (initialProduce) {
                initialProduce = false
                onInitialProduce()
            }
            this.isVisibleToUser.value = isVisibleToUser
            if (offerKey != this.offerKey) {
                this.offerKey = offerKey
                newOffer(offer)
            }
        }

        private fun newOffer(
            offer: FeaturedBundleStore.Bundle
        ) {
            mutateState("newOffer") { state ->
                state.copy(
                    cost = offer.totalDiscountedCost
                        ?: StoreCost(
                            StoreCurrency.ofID(offer.currencyID),
                                amount = offer.itemDiscountedOffers
                                    ?.filter { it.discountedCost.currency.uuid == offer.currencyID }
                                    ?.sumOf { it.discountedCost.amount }
                                ?: offer.itemOffers
                                    .filter { it.baseCost.currency.uuid == offer.currencyID }
                                    .sumOf { it.baseCost.amount }
                        ),
                    durationLeft = offer.durationRemaining
                )
            }

            // TODO: resolve tiers
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
                    snapshotFlow { isVisibleToUser.value }.first { it }
                    if (!initFetchDisplayData()) {
                        // TODO: ask refresh
                        LOOP_BREAK()
                    }
                    if (!initFetchImageData()) {
                        // TODO: ask refresh
                        LOOP_BREAK()
                    }
                    LOOP_BREAK()
                }
            }
        }

        private suspend fun initFetchDisplayData(): Boolean {
            if (isDisplayDataLoaded) return true

            storeClient
                .fetchBundleDataAsync(uuid)
                .awaitOrCancelOnException()
                .onFailure { ex ->
                    onFetchFailure(ex.cast())
                    mutateState("initFetchDisplayData_failure") { state ->
                        state.copy(loadingImage = false, error = true, errorMessage = "Failed to load display data")
                    }
                    return false
                }
                .onSuccess { data -> onFetchBundleDisplayDataSuccess(data) }

            isDisplayDataLoaded = true
            return true
        }

        private suspend fun initFetchImageData(): Boolean {
            if (isImageLoaded) return true

            mutateState("initFetchImageData") { state ->
                state.copy(loadingImage = true)
            }

            assetClient
                .loadBundleImageAsync(LoadBundleImageRequest(uuid, BundleImageType.DISPLAY))
                .awaitOrCancelOnException()
                .onFailure { ex ->
                    onFetchFailure(ex.cast())
                    mutateState("initFetchImageData_failure") { state ->
                        state.copy(loadingImage = false, error = true, errorMessage = "Failed to load Image")
                    }
                    return false
                }
                .onSuccess { data ->
                    onFetchBundleImageSuccess(data)
                }

            isImageLoaded = true
            return true
        }

        private fun onFetchBundleDisplayDataSuccess(
            data: FeaturedBundleDisplayData
        ) {
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "live.store.presentation.dailyoffer.FeaturedBundleDisplayPresenter::StateProducer_onFetchBundleDisplayDataSuccess()"
            )
            mutateState("onFetchBundleDisplayDataSuccess") { state ->
                state.copy(
                    displayName = data.displayName,
                )
            }
        }

        private fun onFetchBundleImageSuccess(
            data: LocalImage<*>
        ) {
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "live.store.presentation.dailyoffer.FeaturedBundleDisplayPresenter::StateProducer_onFetchBundleImageSuccess()"
            )
            mutateState("onFetchBundleImageSuccess") { state ->
                state.copy(
                    loadingImage = false,
                    imageKey = Any(),
                    image = data
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
                "live.store.presentation.dailyoffer.FeaturedBundleDisplayPresenter::StateProducer_onFetchFailure($ex)"
            )
            mutateState("onFetchFailure") { state -> state.UNSET }
        }

        @SnapshotRead
        fun readSnapshot(): FeaturedBundleDisplayState {
            return stateValueOrUnset()
        }

        private fun stateValueOrUnset(): FeaturedBundleDisplayState {
            return _state.value ?: FeaturedBundleDisplayState.UNSET
        }

        private fun mutateState(
            action: String,
            mutate: (FeaturedBundleDisplayState) -> FeaturedBundleDisplayState
        ) {
            checkInMainLooper()
            val current = stateValueOrUnset()
            val new = mutate(current)
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "live.store.presentation.dailyoffer.FeaturedBundleDisplayPresenter: StateProducer_mutateState($action), result=$new"
            )
            _state.value = new
        }
    }
}