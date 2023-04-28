package dev.flammky.valorantcompanion.live.party.presentation

data class PlayerPartyData(
    val matchmakingID: String,
    val members: List<PartyMember>,
    val eligible: List<String>
) {

    companion object {
        val UNSET = PlayerPartyData(
            "",
            emptyList(),
            emptyList()
        )
    }
}
