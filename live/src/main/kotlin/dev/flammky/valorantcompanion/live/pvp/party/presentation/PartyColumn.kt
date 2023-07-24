package dev.flammky.valorantcompanion.live.pvp.party.presentation

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LivePartyColumn(
    state: LivePartyColumnState
) {
    state.partyDataState.value?.let { data ->
        val dataKey = remember(data) { Any() }
        data.members.forEach { info ->

            key(info) {
                PartyColumnMemberCard(
                    modifier = Modifier.height(42.dp),
                    state = rememberPartyColumnMemberCardPresenter().present(
                        memberinfo = info,
                        memberName = state.partyMemberNameLookupResults.value[info.puuid],
                        partyPreferredPods = data.preferredPods,
                        changePreferredPodsKey = dataKey,
                        changePreferredPods = remember(dataKey) {
                            { ids ->
                                state.changePreferredPods(
                                    data, ids
                                )
                            }
                        }
                    )
                )
            }
        }
    }
    LaunchedEffect(
        key1 = state,
        block = {
            snapshotFlow { state.partyDataState.value }
                .collect { _ ->
                    state.lookupName()
                }
        }
    )
}