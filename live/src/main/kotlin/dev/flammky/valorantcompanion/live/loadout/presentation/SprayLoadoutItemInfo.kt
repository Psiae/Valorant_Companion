package dev.flammky.valorantcompanion.live.loadout.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
data class SprayLoadoutItemInfo(
    val uuid: String,
    val displayName: String,
)
