package dev.flammky.valorantcompanion.pvp.store

import kotlinx.collections.immutable.ImmutableSet

data class ValorantStoreEntitledItems(
    val type: ItemType,
    val itemUUIDs: ImmutableSet<String>
)
