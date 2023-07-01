package dev.flammky.valorantcompanion.pvp.mode


// TODO: unrated and competitive is not a Game Mode, both is a "bomb" game mode
sealed class ValorantGameType(
    val displayName: String,
    val queueId: String
) {

    object UNRATED : ValorantGameType(
        displayName = "unrated",
        queueId = "unrated"
    )

    object COMPETITIVE : ValorantGameType(
        displayName = "competitive",
        queueId = "competitive"
    )

    object SWIFTPLAY : ValorantGameType(
        displayName = "swiftplay",
        queueId = "swiftplay"
    )

    object SPIKERUSH : ValorantGameType(
        displayName = "spike rush",
        queueId = "spikerush"
    )

    object DEATHMATCH : ValorantGameType(
        displayName = "deathmatch",
        queueId = "deathmatch"
    )

    object ESCALATION : ValorantGameType(
        displayName = "escalation",
        queueId = "ggteam"
    )

    object TEAM_DEATHMATCH : ValorantGameType(
        displayName = "team deathmatch",
        queueId = "hurm"
    )

    companion object {
        fun fromQueueID(str: String): ValorantGameType? {
            return when(str.lowercase()) {
                UNRATED.queueId -> UNRATED
                COMPETITIVE.queueId -> COMPETITIVE
                SWIFTPLAY.queueId -> SWIFTPLAY
                SPIKERUSH.queueId -> SPIKERUSH
                DEATHMATCH.queueId -> DEATHMATCH
                ESCALATION.queueId -> ESCALATION
                TEAM_DEATHMATCH.queueId -> TEAM_DEATHMATCH
                else -> null
            }
        }

        fun fromQueueIDStrict(str: String): ValorantGameType? {
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
