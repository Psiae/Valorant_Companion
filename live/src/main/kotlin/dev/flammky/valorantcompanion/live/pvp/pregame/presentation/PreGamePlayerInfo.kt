package dev.flammky.valorantcompanion.live.pvp.pregame.presentation

data class PreGamePlayerInfo(
    val puuid: String,
    val cardID: String,
    val titleID: String,
    val accountLevel: Int,
    val preferredLevelBorderID: String,
    val incognito: Boolean,
    val hideAccountLevel: Boolean
) {

    companion object {
        val UNSET = PreGamePlayerInfo(
            puuid = "",
            cardID = "",
            titleID = "",
            accountLevel = 0,
            preferredLevelBorderID = "",
            incognito = false,
            hideAccountLevel = false
        )
    }
}
