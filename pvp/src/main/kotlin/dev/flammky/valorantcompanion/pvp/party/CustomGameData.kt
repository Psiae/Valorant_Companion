package dev.flammky.valorantcompanion.pvp.party

data class CustomGameData(
    val settings: CustomGameDataSettings,
    val membership: CustomGameDataMembership,
    val maxPartySize: Int,
    val autoBalanceEnabled: Boolean,
    val autoBalanceMinPlayers: Boolean,
    val hasRecoveryData: Boolean
)
