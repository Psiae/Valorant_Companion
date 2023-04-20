package dev.flammky.valorantcompanion.auth.riot

data class RiotAccountModel(
    val puuid: String,
    override val username: String,
    val game_name: String,
    val tagline: String
): AccountModel() {

    override val id: String = puuid
}
