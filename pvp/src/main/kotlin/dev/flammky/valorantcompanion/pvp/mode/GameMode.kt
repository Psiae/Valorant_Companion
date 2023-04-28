package dev.flammky.valorantcompanion.pvp.mode

sealed class GameMode(
    val queueId: String
) {

    object UNRATED : GameMode("unrated")

    object COMPETITIVE : GameMode("competitive")

    object SWIFTPLAY : GameMode("swiftplay")

    object SPIKERUSH : GameMode("spikerush")

    object DEATHMATCH : GameMode("deathmatch")

    object ESCALATION : GameMode("ggteam")

    companion object {
        fun parse(str: String): GameMode? {
            return when(str.lowercase()) {
                UNRATED.queueId -> UNRATED
                COMPETITIVE.queueId -> COMPETITIVE
                SWIFTPLAY.queueId -> SWIFTPLAY
                SPIKERUSH.queueId -> SPIKERUSH
                DEATHMATCH.queueId -> DEATHMATCH
                ESCALATION.queueId -> ESCALATION
                else -> null
            }
        }
    }
}
