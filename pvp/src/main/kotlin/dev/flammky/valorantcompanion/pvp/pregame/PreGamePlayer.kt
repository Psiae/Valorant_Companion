package dev.flammky.valorantcompanion.pvp.pregame

data class PreGamePlayer(
    val puuid: String,
    val character_id: String,
    val preGameCharacterSelectionState: PreGameCharacterSelectionState,
    val preGamePlayerState: PreGamePlayerState,
    val competitiveTier: Int,
    val identity: PreGamePlayerIdentity,
    // TODO: Seasonal Badge Info
    val isCaptain: Boolean
)
