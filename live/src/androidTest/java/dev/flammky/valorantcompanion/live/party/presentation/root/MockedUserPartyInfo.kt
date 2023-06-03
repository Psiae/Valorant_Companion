package dev.flammky.valorantcompanion.live.party.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.R
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.live.party.presentation.*
import kotlinx.collections.immutable.persistentListOf

@Composable
fun MockedUserPartyInfo(
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding)
            .background(
                color = run {
                    if (LocalIsThemeDark.current) {
                        val tint = Color.White
                        val surface = Material3Theme.surfaceColorAsState().value
                        remember(tint, surface) {
                            tint
                                .copy(alpha = 0.05f)
                                .compositeOver(surface)
                        }
                    } else {
                        Material3Theme.surfaceColorAsState().value
                    }
                }
            )
            .shadow(elevation = 1.dp)
            .padding(10.dp)
    ) {
        Row {
            Text(
                text = "Party",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Material3Theme.backgroundContentColorAsState().value
            )
            Spacer(modifier = Modifier.weight(2f))
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { },
                painter = painterResource(id = R.drawable.refresh_fill0_wght400_grad0_opsz48),
                contentDescription = "refresh",
                tint = Material3Theme.backgroundContentColorAsState().value
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Divider(
            Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            color = Material3Theme.surfaceVariantColorAsState().value
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PartyColumnMemberCard(
                    modifier = Modifier.height(42.dp),
                    state = PartyColumnMemberCardState(
                        playerCard = null,
                        "Dokka",
                        "301",
                        true,
                        false,
                        persistentListOf(),
                        persistentListOf(),
                        {}
                    )
                )
                Divider(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    color = Material3Theme.surfaceVariantColorAsState().value
                )
                LivePartyMatchmakingColumn(
                    state = LivePartyMatchmakingColumnState(
                        rememberCoroutineScope(),
                        { _, _ -> },
                        {},
                        {}
                    )
                )
            }
        }
    }
}