package dev.flammky.valorantcompanion.pvp.ingame

data class InGameMatchInfo(
    val matchID: String,
    val version: String,
    val mapID: String,
    val provisioningFlow: String,
    val gamePodID: String,
    val gameModeID: String,
    val players: List<InGamePlayer>,
    val queueID: String?,
    val isRanked: Boolean?,
    val matchOver: Boolean
)

/*data class InGameUserMatchData(
    val matchID: String,
    val version: String,
    val map: ValorantMapIdentity,
    val gameType: ValorantGameType,
    val gamePodID: String,
    val gamePodName: String,
    val ally: InGameTeam?,
    val enemy: InGameTeam?,
    val isRanked: Boolean?
)*/
