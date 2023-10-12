package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import android.util.Log
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.player_card.LoadPlayerCardRequest
import dev.flammky.valorantcompanion.assets.player_card.PlayerCardArtType
import dev.flammky.valorantcompanion.assets.spray.LoadSprayImageRequest
import dev.flammky.valorantcompanion.assets.spray.ValorantSprayImageType
import dev.flammky.valorantcompanion.base.ProjectTree
import dev.flammky.valorantcompanion.base.checkInMainLooper
import dev.flammky.valorantcompanion.base.compose.RememberObserver
import dev.flammky.valorantcompanion.base.di.DependencyInjector
import dev.flammky.valorantcompanion.base.di.requireInject
import dev.flammky.valorantcompanion.base.kt.coroutines.awaitOrCancelOnException
import dev.flammky.valorantcompanion.live.BuildConfig
import dev.flammky.valorantcompanion.pvp.store.AccessoryStore
import dev.flammky.valorantcompanion.pvp.store.ItemType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.coroutines.Continuation
import kotlin.time.Duration

interface AccessoryOfferPanelPresenter {

    @Composable
    fun present(
        offerKey: Any,
        offer: AccessoryStore.Offer,
    ): AccessoryOfferPanelState
}

@Composable
fun rememberAccessoryOfferPanelPresenter(
    di: DependencyInjector
): AccessoryOfferPanelPresenter {
    return rememberAccessoryOfferPanelPresenter(
        assetService = di.requireInject()
    )
}

@Composable
fun rememberAccessoryOfferPanelPresenter(
    assetService: ValorantAssetsService
): AccessoryOfferPanelPresenter {
    return remember(assetService) {
        AccessoryOfferPanelPresenterImpl(assetService)
    }
}

private class AccessoryOfferPanelPresenterImpl(
    private val assetService: ValorantAssetsService
) : AccessoryOfferPanelPresenter {

    @Composable
    override fun present(
        offerKey: Any,
        offer: AccessoryStore.Offer,
    ): AccessoryOfferPanelState {
        val producer = remember(this) {
            StateProducer()
        }.apply {
            SideEffect {
                produceParams(offerKey, offer)
            }
        }
        return producer.readSnapshot()
    }


    private inner class StateProducer(

    ): RememberObserver {

        private val _state = mutableStateOf<AccessoryOfferPanelState?>(null)

        private var remembered = false
        private var forgotten = false
        private var abandoned = false
        private var initialProduce = true
        private val lifetime = SupervisorJob()
        private var _coroutineScope: CoroutineScope? = null
        private var _assetClient: ValorantAssetsLoaderClient? = null

        private var producer: Job? = null

        private val producing
            get() = producer?.isActive == true

        private val coroutineScope
            get() = _coroutineScope!!

        private val assetClient
            get() = _assetClient!!

        private var currentLoadCurrencyJob: Job? = null

        private var pendingRefreshContinuations = mutableListOf<Continuation<Unit>>()

        private var _offerKey: Any? = null

        private var _offerRemainingDuration: Duration? = null
        private var _currencies: List<String>? = null
        private var _offerRewards: List<String>? = null

        private val _currenciesImageKeySnapshots = mutableMapOf<String, MutableState<Any>>()
        private val _currenciesImageMap = mutableMapOf<String, LocalImage<*>>()
        private val _currenciesImageWorkers = mutableMapOf<String, Job>()

        private val _rewardItemTypeSnapshots = mutableMapOf<String, MutableState<ItemType>>()
        private val _rewardImageKeySnapshots = mutableMapOf<String, MutableState<Any>>()
        private val _rewardImageMap = mutableMapOf<String, LocalImage<*>>()
        private val _rewardImageWorkers = mutableMapOf<String, Job>()

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
            assetClient.dispose()
        }

        override fun onRemembered() {
            super.onRemembered()
            check(!remembered)
            check(!forgotten)
            check(!abandoned)
            remembered = true
            _coroutineScope = CoroutineScope(Dispatchers.Main + lifetime)
            _assetClient = assetService.createLoaderClient()
        }

        fun produceParams(
            offerKey: Any,
            offer: AccessoryStore.Offer
        ) {
            produceParamsProducerBehaviorCheck()

            if (initialProduce) {
                initialProduce = false
                onInitialProduce()
            }
            invalidateParams(
                offerKey,
                offer
            )
        }

        fun readSnapshot() : AccessoryOfferPanelState {
            return stateValueOrUnset()
        }

        private fun produceParamsProducerBehaviorCheck() {
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
        }

        private fun stateValueOrUnset(): AccessoryOfferPanelState {
            return _state.value ?: AccessoryOfferPanelState.UNSET
        }

        private fun invalidateParams(
            offerKey: Any,
            offer: AccessoryStore.Offer
        ) {
            if (_offerKey != offerKey) {
                _offerKey = offerKey
                onNewOffer(offer)
            }
        }

        private fun onNewOffer(
            offer: AccessoryStore.Offer
        ) {
            val (durationChanged, duration) = run {
                Pair(
                    offer.remainingDuration != _offerRemainingDuration,
                    offer.remainingDuration
                )
            }
            val (currenciesChanged, currencies) = run {
                val currencies = offer.offers.map { it.value.cost.currency.uuid }.distinct()
                Pair(
                    currencies != _currencies,
                    currencies
                )
            }
            val (offerRewardChanged, offerReward) = run {
                val offerReward = offer.offers.mapNotNull { it.value.rewards.firstOrNull()?.itemID }
                Pair(
                    offerReward != _offerRewards,
                    offerReward
                )
            }
            if (durationChanged) {
                onNewOfferDuration(duration)
            }
            if (currenciesChanged) {
                onNewCurrencies(currencies)
            }
            if (offerRewardChanged) {
                onNewOfferRewards(
                    offerReward,
                    offer.offers.entries
                        .mapNotNull {
                            it.value.rewards.firstOrNull()?.let { reward ->
                                reward.itemID to reward.itemType
                            }
                        }
                        .toMap()
                )
            }
        }

        private fun onNewOfferDuration(
            duration: Duration
        ) {
            this._offerRemainingDuration = duration
            mutateState("onNewOfferDuration") { state ->
                state.copy(
                    durationLeft = duration
                )
            }
        }

        private fun onNewCurrencies(
            currencies: List<String>
        ) {
            this._currencies = currencies
            filterCurrencySnapshots()
            dispatchCurrencyWorkers()
            mutateState("onNewCurrencies") { state ->
                state.copy(
                    currencyCount = currencies.size,
                    getCurrencyImageKey = { i ->
                        _currenciesImageKeySnapshots[currencies[i]]!!.value
                    },
                    getCurrencyImage = { i ->
                        _currenciesImageMap[currencies[i]]
                            ?: state.UNSET.getCurrencyImage(i)
                    }
                )
            }
        }

        private fun filterCurrencySnapshots() {
            val currencies = this._currencies
                ?: return
            mutableMapOf<String, MutableState<Any>>()
                .apply {
                    val initKey = Any()
                    currencies.forEach { id ->
                        val snapshot = _currenciesImageKeySnapshots[id] ?: mutableStateOf<Any>(initKey)
                        put(id, snapshot)
                    }
                }
                .run {
                    _currenciesImageKeySnapshots.forEach { entry ->
                        if (!containsKey(entry.key)) {
                            _currenciesImageMap.remove(entry.key)
                            _currenciesImageWorkers.remove(entry.key)
                                ?.apply {
                                    cancel()
                                }
                        }
                    }
                    _currenciesImageKeySnapshots.clear()
                    _currenciesImageKeySnapshots.putAll(this)
                }
        }

        private fun dispatchCurrencyWorkers() {
            _currenciesImageKeySnapshots.forEach { entry ->
                _currenciesImageWorkers
                    .apply {
                        if (!containsKey(entry.key)) {
                            put(entry.key, currencyImageWorker(entry.key))
                        }
                    }
            }
        }

        private fun currencyImageWorker(
            currencyId: String
        ): Job {
            return coroutineScope.launch {
                assetClient
                    .loadCurrencyImageAsync(currencyId)
                    .awaitOrCancelOnException()
                    .fold(
                        onSuccess = { data ->
                            Log.d("DBG", "currencyImageWorker($currencyId)_success($data)")
                            _currenciesImageKeySnapshots[currencyId]!!.apply {
                                _currenciesImageMap[currencyId] = data
                                value = Any()
                            }
                        },
                        onFailure = { ex ->
                            Log.d("DBG", "currencyImageWorker($currencyId)_failure($ex)")
                            // TODO
                        }
                    )
            }
        }

        private fun onNewOfferRewards(
            rewards: List<String>,
            rewardsItemType: Map<String, ItemType>
        ) {
            this._offerRewards = rewards
            filterOfferRewardsSnapshots(rewardsItemType)
            dispatchOfferRewardsWorkers()
            mutateState("onNewRewards") { state ->
                state.copy(
                    offerCount = rewards.size,
                    getOfferDisplayImageKey = { i ->
                        _rewardImageKeySnapshots[rewards[i]]!!.value
                    },
                    getOfferDisplayImage = { i ->
                        _rewardImageMap[rewards[i]]
                            ?: state.UNSET.getCurrencyImage(i)
                    }
                )
            }
        }

        private fun filterOfferRewardsSnapshots(
            types: Map<String, ItemType>
        ) {
            val rewards = this._offerRewards
                ?: return
            mutableMapOf<String, MutableState<Any>>()
                .apply {
                    val initKey = Any()
                    rewards.forEach { item ->
                        val snapshot = _rewardImageKeySnapshots[item] ?: mutableStateOf<Any>(initKey)
                        put(item, snapshot)
                    }
                }
                .run {
                    _rewardImageKeySnapshots.forEach { entry ->
                        if (!containsKey(entry.key)) {
                            _rewardImageMap.remove(entry.key)
                            _rewardImageWorkers.remove(entry.key)
                                ?.apply {
                                    cancel()
                                }
                        }
                    }
                    _rewardImageKeySnapshots.clear()
                    _rewardImageKeySnapshots.putAll(this)
                }

            mutableMapOf<String, MutableState<ItemType>>()
                .apply {
                    types.forEach { type ->
                        val snapshot = _rewardItemTypeSnapshots[type.key]
                            ?.apply { value = type.value }
                            ?: mutableStateOf(type.value)
                        put(type.key, snapshot)
                    }
                }
                .run {
                    _rewardItemTypeSnapshots.clear()
                    _rewardItemTypeSnapshots.putAll(this)
                }
        }

        private fun dispatchOfferRewardsWorkers() {
            _rewardImageKeySnapshots.forEach { entry ->
                _rewardImageWorkers
                    .apply {
                        if (!containsKey(entry.key)) {
                            put(entry.key, rewardImageWorker(entry.key))
                        }
                    }
            }
        }

        private fun rewardImageWorker(
            rewardID: String,
        ): Job {
            return coroutineScope.launch {
                var task: Job? = null
                snapshotFlow { _rewardItemTypeSnapshots[rewardID]!!.value }
                    .distinctUntilChanged()
                    .collect { type ->
                        Log.d("DEBUG", "rewardImageWorker, rewardID=$rewardID, type=$type")
                        task?.cancel()
                        task = launch {
                            // TODO
                            when(type) {
                                ItemType.GunBuddy -> assetClient.loadGunBuddyImageAsync(rewardID)
                                ItemType.PlayerCard -> assetClient.loadUserPlayerCardImageAsync(
                                    LoadPlayerCardRequest(rewardID, PlayerCardArtType.SMALL)
                                )
                                ItemType.Spray -> assetClient.loadSprayImageAsync(
                                    LoadSprayImageRequest(
                                        rewardID,
                                        ValorantSprayImageType.DISPLAY_ICON,
                                        ValorantSprayImageType.FULL_ICON(true)
                                    )
                                )
                                else -> {
                                    _rewardImageKeySnapshots[rewardID]?.apply {
                                        _rewardImageMap[rewardID] = LocalImage.None
                                        value = LocalImage.None
                                    }
                                    return@launch
                                }
                            }.awaitOrCancelOnException().fold(
                                onSuccess = { data ->
                                    Log.d("DBG", "rewardImageWorker($rewardID)_success($data)")
                                    _rewardImageKeySnapshots[rewardID]?.apply {
                                        _rewardImageMap[rewardID] = data
                                        value = Any()
                                    }
                                },
                                onFailure = { ex ->
                                    Log.d("DBG", "rewardImageWorker($rewardID)_failure($ex)")
                                    // TODO
                                }
                            )
                        }
                    }
            }
        }

        private fun onInitialProduce() {
            mutateState("onInitialProduce") { state ->
                state.UNSET
            }
        }

        private fun mutateState(
            action: String,
            mutate: (AccessoryOfferPanelState) -> AccessoryOfferPanelState
        ) {
            checkInMainLooper()
            val current = stateValueOrUnset()
            val new = mutate(current)
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "${ProjectTree.packageName}.AccessoryOfferPanelPresenter: StateProducer_mutateState($action), result=$new"
            )
            if (current === new) return
            _state.value = new
        }
    }
}