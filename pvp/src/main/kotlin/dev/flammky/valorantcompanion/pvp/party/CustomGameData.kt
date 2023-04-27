package dev.flammky.valorantcompanion.pvp.party

data class CustomGameData(
    val settings: CustomGameDataSettings,
    val membership: CustomGameDataMembership,
    val maxPartySize: Int,
    val autobalanceEnabled: Boolean,
    val autobalanceMinPlayers: Int,
    val hasRecoveryData: Boolean
)
