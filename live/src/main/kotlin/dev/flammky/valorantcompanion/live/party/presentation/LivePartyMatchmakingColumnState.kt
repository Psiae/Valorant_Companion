package dev.flammky.valorantcompanion.live.party.presentation

import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.pvp.party.PartyChangeQueueRequest
import dev.flammky.valorantcompanion.pvp.party.PartyService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import org.koin.androidx.compose.get as getFromKoin


class LivePartyMatchmakingColumnState(
    private val coroutineScope: CoroutineScope,
    changeMatchmakingQueue: (partyID: String, queueID: String) -> Unit
) {

    private val _changeMatchmakingQueue = changeMatchmakingQueue

    var isUserOwner by mutableStateOf<Boolean>(false)
    var queueID by mutableStateOf<String?>(null)
    var eligibleQueues by mutableStateOf<ImmutableList<String>?>(null)
    var members by mutableStateOf<ImmutableList<PlayerPartyMemberInfo>?>(null)
    var partyID by mutableStateOf<String?>(null)

    fun changeMatchmakingQueue(
        partyID: String,
        queueID: String
    ) = _changeMatchmakingQueue(partyID, queueID)
}

class LivePartyMatchmakingColumnPresenter(
    private val userPUUID: String,
    private val pvpService: PartyService
) {

    @Composable
    fun present(
        partyData: PlayerPartyData
    ): LivePartyMatchmakingColumnState {
        val coroutineScope = rememberCoroutineScope()
        val client = remember(this) { pvpService.createClient() }
        return remember(this) {
            LivePartyMatchmakingColumnState(
                coroutineScope,
                changeMatchmakingQueue = { pID, qID ->
                    client.changePartyMatchmakingQueue(
                        PartyChangeQueueRequest(
                            puuid = userPUUID,
                            partyID = pID,
                            queueID = qID
                        ) {

                        }
                    )
                }
            )
        }.apply {
            partyID = partyData.partyID
            isUserOwner = partyData.members
                .find { it.isOwner }
                ?.let { owner -> owner.puuid == userPUUID } == true
            queueID = partyData.matchmakingQueueID
            eligibleQueues = partyData.eligible
            members = partyData.members
        }
    }
}

@Composable
fun rememberLivePartyMatchmakingColumnPresenter(
    userPUUID: String,
    partyService: PartyService = getFromKoin()
): LivePartyMatchmakingColumnPresenter {
    return remember(userPUUID, partyService) { LivePartyMatchmakingColumnPresenter(userPUUID, partyService) }
}