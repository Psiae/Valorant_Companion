package dev.flammky.valorantcompanion.live.party.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.assets.PlayerCardArtType
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.internal.LoadPlayerCardRequest
import dev.flammky.valorantcompanion.pvp.party.PartyService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get as getFromKoin

@Composable
fun PartyColumn(
    state: LivePartyState
) {
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxWidth()) {
        state.partyMembers.value.forEach { member ->
            key(member.puuid) {
                val upMember = remember {
                    mutableStateOf(member)
                }
                val cardArt = remember(upMember.value) {
                    mutableStateOf<Any?>(null)
                }
                DisposableEffect(
                    cardArt
                ) {
                    val supervisor = SupervisorJob()
                    coroutineScope.launch(supervisor) {
                        var job: Job? = null
                        snapshotFlow { upMember.value }
                            .collect {
                                job?.cancel()
                                job = launch {
                                    cardArt.value = state.loadPlayerCardArtAsset(
                                        it.cardArtId,
                                        PlayerCardArtType.SMALL
                                    ).await()
                                }
                            }
                    }
                    onDispose { supervisor.cancel() }
                }
                PartyColumnMemberCard(
                    modifier = Modifier.height(56.dp),
                    remember(member) { PartyColumnMemberCardState(cardArt.value, member.name, member.tag) }
                )
            }
        }
    }
}

@Composable
fun rememberLivePartyPresenter(
    assetsService: ValorantAssetsService = getFromKoin(),
    partyService: PartyService = getFromKoin()
): LivePartyPresenter {
    return remember(assetsService, partyService) { LivePartyPresenter(assetsService, partyService) }
}

class LivePartyState(
    val loadPlayerCardArtAsset: (
        id: String,
        type: PlayerCardArtType
    ) -> Deferred<Any>
) {
    val partyMembers = mutableStateOf<List<PartyMember>>(emptyList())
}

class LivePartyPresenter(
    private val assetsService: ValorantAssetsService,
    private val partyService: PartyService
) {

    @Composable
    fun present(): LivePartyState {
        val partyClient = remember(this) {
            partyService.createClient()
        }
        val assetClient = remember(this) {
            assetsService.createLoaderClient()
        }
        val state = remember(assetClient, partyClient) {
            LivePartyState(
                loadPlayerCardArtAsset = { id, type ->
                    assetClient.loadUserPlayerCardAsync(
                        LoadPlayerCardRequest(id, type)
                    )
                }
            )
        }
        DisposableEffect(
            this
        ) {
            partyService
            onDispose { partyClient.dispose() ; assetClient.dispose() }
        }
        return state
    }
}