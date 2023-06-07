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
) {

    companion object {
        val UNSET by lazy {
            PreGamePlayer(
                puuid = "",
                characterID = "",
                characterSelectionState = CharacterSelectionState.NONE,
                pregamePlayerState = PreGamePlayerState.ELSE,
                competitiveTier = 0,
                identity = PreGamePlayerInfo.UNSET,
                seasonalBadgeInfo = SeasonalBadgeInfo.UNSET,
                isCaptain = false
            )
        }
    }
}

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
