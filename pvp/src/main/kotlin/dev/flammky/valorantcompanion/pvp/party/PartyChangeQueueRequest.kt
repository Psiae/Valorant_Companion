package dev.flammky.valorantcompanion.pvp.party

class PartyChangeQueueRequest private constructor(
    val puuid: String,
    val partyID: String,
    val queueID: String
) {


    class Builder(
        puuid: String,
        partyID: String,
        queueID: String
    ) {
        private var _puuid = puuid
        private var _partyID = partyID
        private var _queueID = queueID

        fun build(): PartyChangeQueueRequest = PartyChangeQueueRequest(
            _puuid,
            _partyID,
            _queueID
        )
    }
}


fun PartyChangeQueueRequest(
    puuid: String,
    partyID: String,
    queueID: String,
    build: PartyChangeQueueRequest.Builder.() -> Unit
): PartyChangeQueueRequest {
    return PartyChangeQueueRequest.Builder(puuid, partyID, queueID).apply(build).build()
}

class PartyChangeQueueRequestResult(
    val newData: PlayerPartyData
)
