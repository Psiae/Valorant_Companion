package dev.flammky.valorantcompanion.live.pregame.presentation

data class PreGamePlayer(
    val puuid: String,
    val characterID: String,
    val characterSelectionState: CharacterSelectionState,
    val pregamePlayerState: PreGamePlayerState,
    val competitiveTier: Int,
    // competitiveEP: Int /* realistically we only display the latest EP */
    val identity: PreGamePlayerInfo,
    val seasonalBadgeInfo: SeasonalBadgeInfo,
    val isCaptain: Boolean
)

enum class PreGamePlayerState {
    JOINED,
    // Unknown
    ELSE
}

enum class CharacterSelectionState {
    NONE,
    SELECTED,
    LOCKED
}
