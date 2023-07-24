package dev.flammky.valorantcompanion.live.pvp.party.presentation

import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.pvp.party.PartyChangeQueueRequest
import dev.flammky.valorantcompanion.pvp.party.PartyService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration
import org.koin.androidx.compose.get as getFromKoin


class LivePartyMatchmakingColumnState(
    private val coroutineScope: CoroutineScope,
    changeMatchmakingQueue: (partyID: String, queueID: String) -> Unit,
    enterMatchmakingQueue: (partyID: String) -> Unit,
    quitMatchmakingQueue: (partyID: String) -> Unit
) {

    private val _changeMatchmakingQueue = changeMatchmakingQueue
    private val _enterMatchmakingQueue = enterMatchmakingQueue
    private val _quitMatchmakingQueue = quitMatchmakingQueue

    var isUserOwner by mutableStateOf<Boolean>(false)
    var queueID by mutableStateOf<String?>(null)
    var eligibleQueues by mutableStateOf<ImmutableList<String>?>(null)
    var members by mutableStateOf<ImmutableList<PlayerPartyMemberInfo>?>(null)
    var partyID by mutableStateOf<String?>(null)
    var inQueue by mutableStateOf<Boolean>(false)
    var timeStamp by mutableStateOf<Duration?>(null)

    fun changeMatchmakingQueue(
        partyID: String,
        queueID: String
    ) = _changeMatchmakingQueue(partyID, queueID)

    fun enterMatchmakingQueue(
        partyID: String,
    ) = _enterMatchmakingQueue(partyID)

    fun quitMatchmakingQueue(
        partyID: String,
    ) = _quitMatchmakingQueue(partyID)
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
                },
                enterMatchmakingQueue = { pID ->
                    client.partyJoinMatchmaking(userPUUID, pID)
                },
                quitMatchmakingQueue = { pID ->
                    client.partyLeaveMatchmaking(userPUUID, pID)
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
            inQueue = partyData.inQueue
            timeStamp = partyData.timeStamp
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