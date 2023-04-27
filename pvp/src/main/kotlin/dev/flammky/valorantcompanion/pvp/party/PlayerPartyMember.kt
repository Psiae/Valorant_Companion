package dev.flammky.valorantcompanion.pvp.party

import dev.flammky.valorantcompanion.pvp.party.internal.PlayerPartyMemberIdentity

data class PlayerPartyMember(
    val uuid: String,
    val competitiveTier: Int,
    val identity: PlayerPartyMemberIdentity,
    val seasonalBadgeInfo: Any? = null,
    val isOwner: Boolean? = null,
    val queueEligibleRemainingAccountLevels: Int,
    val pings: List<PlayerPartyMemberPing>,
    val isReady: Boolean,
    val isModerator: Boolean,
    val useBroadcastHUD: Boolean,
    val platformType: String
) {
}