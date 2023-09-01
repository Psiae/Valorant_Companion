package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import dev.flammky.valorantcompanion.base.UNSET
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class DailyOfferState(
    val offersKey: Any,
    val offers: ImmutableList<String>
): UNSET<DailyOfferState> by Companion {

    companion object : UNSET<DailyOfferState> {


        override val UNSET: DailyOfferState = DailyOfferState(
            Any(),
            persistentListOf()
        )
    }

}