package dev.flammky.valorantcompanion.assets.internal

import dev.flammky.valorantcompanion.assets.PlayerCardArtType
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentSet

class LoadPlayerCardRequest internal constructor(
    val player_card_id: String,
    // ordered
    val acceptableTypes: PersistentSet<PlayerCardArtType>,
) {

    public constructor(
        player_card_id: String,
        vararg acceptableTypes: PlayerCardArtType,
    ) : this(player_card_id, acceptableTypes.asIterable().toPersistentSet())
}