package dev.flammky.valorantcompanion.live.loadout.presentation

import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.pvp.loadout.SprayLoadoutItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.util.Objects

@Stable
@Immutable
data class SprayLoadoutPickerState(
    // key that represent possible change of activeSprays
    // it does not represent the structure
    val activeSpraysKey: Any,
    val activeSprays: ImmutableList<SprayLoadoutItem>
): UNSET<SprayLoadoutPickerState> by Companion {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is SprayLoadoutPickerState) return false

        // same key should ensure same content,
        // different key does not always mean different content
        if (other.activeSpraysKey != activeSpraysKey) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(activeSpraysKey)
    }

    companion object : UNSET<SprayLoadoutPickerState> {

        override val UNSET: SprayLoadoutPickerState = SprayLoadoutPickerState(
            activeSpraysKey = Any(),
            activeSprays = persistentListOf()
        )
    }
}