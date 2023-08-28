package dev.flammky.valorantcompanion.live.loadout.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.pvp.loadout.SprayLoadoutItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import java.util.Objects

@Stable
@Immutable
data class SprayLoadoutPickerDetailScreenState(
    val slotEquipIdsKey: Any,
    val slotEquipIds: ImmutableList<String>,
    // TODO: consider equipID instead
    val selectedSlotEquipIndex: Int,
    // TODO: consider using snapshot read lambda
    val slotContentsKey: Any,
    val slotContents: ImmutableMap<String, SprayLoadoutItem>,
    val hasSelectedSlotSprayDisplayName: Boolean,
    val selectedSlotSprayDisplayName: String,
    val selectedReplacementSprayUUID: String?,
    val hasSelectedReplacementSprayDisplayName: Boolean,
    val selectedReplacementSprayDisplayName: String,
    val selectSpray: (String) -> Unit,
    val canReplaceSpray: Boolean,
    val confirmReplaceSpray: () -> Unit,
    val explicitLoading: Boolean,
    val explicitLoadingMessage: String?
    // TODO: errors
) : UNSET<SprayLoadoutPickerDetailScreenState> by Companion {

    // same key should ensure same content,
    // different key does not always mean different content
    // although content may be the same, the key equals is false
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SprayLoadoutPickerDetailScreenState) return false

        if (this.slotEquipIdsKey != other.slotEquipIdsKey) return false
        if (this.selectedSlotEquipIndex != other.selectedSlotEquipIndex) return false

        if (this.slotContentsKey != other.slotContentsKey) return false

        if (this.hasSelectedSlotSprayDisplayName != other.hasSelectedSlotSprayDisplayName) return false
        if (this.selectedSlotSprayDisplayName != other.selectedSlotSprayDisplayName) return false
        if (this.selectedReplacementSprayUUID != other.selectedReplacementSprayUUID) return false
        if (this.hasSelectedReplacementSprayDisplayName != other.hasSelectedReplacementSprayDisplayName) return false
        if (this.selectedReplacementSprayDisplayName != other.selectedReplacementSprayDisplayName) return false

        if (this.selectSpray != other.selectSpray) return false

        if (this.canReplaceSpray != other.canReplaceSpray) return false
        if (this.confirmReplaceSpray != other.confirmReplaceSpray) return false

        if (this.explicitLoading != other.explicitLoading) return false
        if (this.explicitLoadingMessage != other.explicitLoadingMessage) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(
            slotEquipIdsKey,
            selectedSlotEquipIndex,
            slotContentsKey,
            hasSelectedSlotSprayDisplayName,
            selectedSlotSprayDisplayName,
            selectedReplacementSprayUUID,
            hasSelectedReplacementSprayDisplayName,
            selectedReplacementSprayDisplayName,
            canReplaceSpray,
            selectSpray,
            confirmReplaceSpray,
            explicitLoading,
            explicitLoadingMessage
        )
    }

    companion object : UNSET<SprayLoadoutPickerDetailScreenState> {

        override val UNSET: SprayLoadoutPickerDetailScreenState = SprayLoadoutPickerDetailScreenState(
            slotEquipIdsKey = Any(),
            slotEquipIds = persistentListOf(),
            selectedSlotEquipIndex = -1,
            slotContentsKey = Any(),
            slotContents = persistentMapOf(),
            hasSelectedSlotSprayDisplayName = false,
            selectedSlotSprayDisplayName = "",
            selectedReplacementSprayUUID = null,
            hasSelectedReplacementSprayDisplayName = false,
            selectedReplacementSprayDisplayName = "",
            selectSpray = {},
            canReplaceSpray = false,
            confirmReplaceSpray = {},
            explicitLoading = false,
            explicitLoadingMessage = null
        )
    }
}

val SprayLoadoutPickerDetailScreenState.selectedSlotSpray
    get() = slotContents[slotEquipIds.getOrNull(selectedSlotEquipIndex)]
