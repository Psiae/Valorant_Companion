package dev.flammky.valorantcompanion.pvp.party

data class PlayerPartyInfo(
    val subject: String,
    val version: Int,
    val currentPartyId: String,
    val invites: Any? = null,
    val requests: List<PartyInviteRequest>,
    val platformInfo: PlatformInfo
) {
}