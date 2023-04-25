package dev.flammky.valorantcompanion.pvp.party

data class MatchmakingData(
    val queueId: String,
    val preferredGamePods: String,
    val skillDisparityRRPenalty: String
) {
}