package dev.flammky.valorantcompanion.live.party.presentation

data class PartyMember(
    val puuid: String,
    val cardArtId: String,
    val name: String,
    val tag: String,
    val isOwner: Boolean,
    val isReady: Boolean,
    val gamePods: List<GamePod>
) {
}