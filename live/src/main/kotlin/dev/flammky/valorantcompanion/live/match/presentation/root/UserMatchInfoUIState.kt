package dev.flammky.valorantcompanion.live.match.presentation.root

import androidx.compose.runtime.Immutable

@Immutable
data class UserMatchInfoUIState(
    val inPreGame: Boolean,
    val inGame: Boolean,
    val mapName: String,
    val gameModeName: String,
    val gamePodName: String,
    val gamePodPingMs: Int
)