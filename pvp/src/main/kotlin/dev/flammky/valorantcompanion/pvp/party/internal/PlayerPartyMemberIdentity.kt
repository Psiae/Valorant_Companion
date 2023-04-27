package dev.flammky.valorantcompanion.pvp.party.internal

data class PlayerPartyMemberIdentity(
    val uuid: String,
    val cardId: String,
    val titleId: String,
    val accountLevel: Int,
    val preferred_level_border_id: String = "",
    val incognito: Boolean,
    val hideAccountLevel: Boolean
) {
}