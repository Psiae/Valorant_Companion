package dev.flammky.valorantcompanion.pvp.mode

sealed class ValorantGameMode(
    val displayName: String,
    val queueId: String
) {

    object UNRATED : ValorantGameMode(
        displayName = "unrated",
        queueId = "unrated"
    )

    object COMPETITIVE : ValorantGameMode(
        displayName = "competitive",
        queueId = "competitive"
    )

    object SWIFTPLAY : ValorantGameMode(
        displayName = "swiftplay",
        queueId = "swiftplay"
    )

    object SPIKERUSH : ValorantGameMode(
        displayName = "spike rush",
        queueId = "spikerush"
    )

    object DEATHMATCH : ValorantGameMode(
        displayName = "deathmatch",
        queueId = "deathmatch"
    )

    object ESCALATION : ValorantGameMode(
        displayName = "escalation",
        queueId = "ggteam"
    )

    companion object {
        fun fromQueueID(str: String): ValorantGameMode? {
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

        fun fromQueueIDStrict(str: String): ValorantGameMode? {
            return when(str) {
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
