package dev.flammky.valorantcompanion.live.loadout.presentation

import dev.flammky.valorantcompanion.base.UNSET
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import java.util.Objects

data class SprayLoadoutPickerPoolState(
    val ownedSpraysKey: Any,
    val ownedSprays: ImmutableList<String>
): UNSET<SprayLoadoutPickerPoolState> by Companion {

    override fun equals(other: Any?): Boolean {
        return this === other ||
                other is SprayLoadoutPickerPoolState &&
                ownedSpraysKey == other.ownedSpraysKey
    }

    override fun hashCode(): Int {
        return Objects.hash(ownedSpraysKey)
    }

    companion object : UNSET<SprayLoadoutPickerPoolState> {

        override val UNSET: SprayLoadoutPickerPoolState = SprayLoadoutPickerPoolState(
            Any(),
            persistentListOf()
        )
    }
}