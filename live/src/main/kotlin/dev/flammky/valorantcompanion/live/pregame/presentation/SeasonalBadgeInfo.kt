package dev.flammky.valorantcompanion.live.pregame.presentation

data class SeasonalBadgeInfo(
    val seasonID: String,
    val numberOfWins: Int,
    val winsByTier: Any?,
    val rank: Int,
    val leaderBoardRank: Int
)
