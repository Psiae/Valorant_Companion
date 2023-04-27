package dev.flammky.valorantcompanion.pvp.party

data class MatchmakingData(
    val queueId: String,
    val preferredGamePods: List<String>,
    val skillDisparityRRPenalty: String
) {
}