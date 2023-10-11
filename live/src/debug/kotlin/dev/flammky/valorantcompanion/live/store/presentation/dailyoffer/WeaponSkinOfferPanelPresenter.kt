package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.base.checkInMainLooper
import dev.flammky.valorantcompanion.base.compose.RememberObserver
import dev.flammky.valorantcompanion.live.BuildConfig
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.cpreview.WeaponSkinOfferPanelItem
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.cpreview.WeaponSkinOfferPanelState
import dev.flammky.valorantcompanion.pvp.store.ItemType
import dev.flammky.valorantcompanion.pvp.store.SkinsPanelStore
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

interface WeaponSkinOfferPanelPresenter {

    @Composable
    fun present(
        offerKey: Any,
        offer: SkinsPanelStore.Offer
    ): WeaponSkinOfferPanelState
}

class WeaponSkinOfferPanelPresenterImpl() : WeaponSkinOfferPanelPresenter {

    @Composable
    override fun present(offerKey: Any, offer: SkinsPanelStore.Offer): WeaponSkinOfferPanelState {
        TODO("Not yet implemented")
    }

    private inner class StateProducer() : RememberObserver {

        private val _state = mutableStateOf<WeaponSkinOfferPanelState?>(null)

        private var remembered = false
        private var forgotten = false
        private var abandoned = false

        private var offerKey: Any? = null
        private var lastReward: SkinsPanelStore.Reward? = null

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
        }

        override fun onRemembered() {
            super.onRemembered()
            check(!remembered)
            check(!forgotten)
            check(!abandoned)
            remembered = true
        }

        fun produce(
            offerKey: Any,
            offer: SkinsPanelStore.Offer
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
            invalidateParams(offerKey, offer)
        }

        fun invalidateParams(
            offerKey: Any,
            offer: SkinsPanelStore.Offer
        ) {
            if (this.offerKey != offerKey) {
                this.offerKey = offerKey
                newOffer(offer)
            }
        }

        fun readSnapshot(): WeaponSkinOfferPanelState {
            return stateValueOrUnset()
        }

        private fun stateValueOrUnset(): WeaponSkinOfferPanelState {
            return _state.value ?: WeaponSkinOfferPanelState.UNSET
        }

        private fun newOffer(offer: SkinsPanelStore.Offer) {
            mutateState("newOffer") { state ->
                state.copy(
                    remainingDuration = offer.remainingDuration,
                    itemsKey = Any(),
                    items = offer.itemOffers.mapTo(
                        persistentListOf<WeaponSkinOfferPanelItem>().builder(),
                        transform = { entry ->
                            val value = entry.value
                            WeaponSkinOfferPanelItem(
                                uuid = entry.key,
                                cost = value.cost,
                            )
                        }
                    ).build()
                )
            }
        }

        private fun invalidateReward(reward: SkinsPanelStore.Reward) {
            if (reward != lastReward) {
                newReward(reward)
            }
        }

        private fun newReward(reward: SkinsPanelStore.Reward) {
            val old = lastReward
            lastReward = reward
            if (reward.itemID != old?.itemID) {
                newRewardItemID(reward.itemID, type = reward.itemType)
            }
        }

        private fun newRewardItemID(
            id: String,
            type: ItemType
        ) {

        }

        private fun mutateState(
            action: String,
            mutate: (WeaponSkinOfferPanelState) -> WeaponSkinOfferPanelState
        ) {
            checkInMainLooper()
            val current = stateValueOrUnset()
            val new = mutate(current)
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "live.store.presentation.dailyoffer.WeaponSkinOfferPanelPresenter: StateProducer_mutateState($action), result=$new"
            )
            _state.value = new
        }
    }
}