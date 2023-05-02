package dev.flammky.valorantcompanion.pvp.party

class PartyChangePreferredPodsRequest private constructor(
    val puuid: String,
    val partyID: String,
    val preferredPods: Set<String>
) {


    class Builder(
        puuid: String,
        partyID: String,
        preferredPods: Set<String>
    ) {
        private var _puuid = puuid
        private var _partyID = partyID
        private var _preferredPods = preferredPods

        fun build(): PartyChangePreferredPodsRequest = PartyChangePreferredPodsRequest(
            _puuid,
            _partyID,
            _preferredPods
        )
    }
}


fun PartyChangePreferredPodsRequest(
    puuid: String,
    partyID: String,
    preferredPods: Set<String>,
    build: PartyChangePreferredPodsRequest.Builder.() -> Unit
): PartyChangePreferredPodsRequest {
    return PartyChangePreferredPodsRequest.Builder(puuid, partyID, preferredPods).apply(build).build()
}

class PartyChangePreferredPodsRequestResult(
    val newData: PlayerPartyData
)