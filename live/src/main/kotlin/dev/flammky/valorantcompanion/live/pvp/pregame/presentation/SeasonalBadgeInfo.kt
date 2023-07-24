package dev.flammky.valorantcompanion.live.pvp.pregame.presentation

data class SeasonalBadgeInfo(
    val seasonID: String,
    val numberOfWins: Int,
    val winsByTier: Any?,
    val rank: Int,
    val leaderBoardRank: Int
) {

    companion object {

        val UNSET by lazy {
            SeasonalBadgeInfo(
                "",
                0,
                null,
                0,
                0
            )
        }
    }
}
