package dev.flammky.valorantcompanion.live.pvp.party.presentation

data class PlayerPartyMemberInfo(
    val puuid: String,
    val cardArtId: String,
    val isLeader: Boolean,
    val isReady: Boolean,
    val gamePodData: List<GamePodConnection>,
)

data class PlayerPartyMemberName(
    val puuid: String,
    val name: String,
    val tag: String
)