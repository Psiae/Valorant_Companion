package dev.flammky.valorantcompanion.live.pregame.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.base.runRemember
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark
import dev.flammky.valorantcompanion.live.shared.presentation.LocalImageData

@Composable
fun AgentSelectionPlayerCard(
    state: AgentSelectionPlayerCardState
) = AgentSelectionPlayerCardPlacement(
    titleAvailable = state.playerGameName.isNotEmpty(),
    agentPicture = { modifier ->
        AgentPicture(
            modifier = modifier,
            res = state.selectedAgentIcon,
            key = state.selectedAgentIconKey,
            agentName = state.selectedAgentName
        )
    },
    title = { modifier ->
        PlayerNameText(
            modifier = modifier,
            gameName = state.playerGameName,
            tag = state.playerGameNameTag,
            isUser = state.isUser
        )
    },
    subtitle = { modifier ->
        AgentIdentity(
            modifier = modifier,
            agentName = state.selectedAgentName,
            roleName = state.selectedAgentRoleName,
            roleIcon = state.selectedAgentRoleIcon,
            roleIconKey = state.selectedAgentRoleIconKey,
            lockedIn = state.isLockedIn
        )
    },
    competitiveTierIcon = { modifier ->
        CompetitiveTierIcon(
            modifier = modifier,
            res = state.tierIcon,
            key = state.tierIconKey,
            tierName = state.tierName
        )
    },
)

@Composable
private fun AgentSelectionPlayerCardPlacement(
    titleAvailable: Boolean,
    agentPicture: @Composable (Modifier) -> Unit,
    title: @Composable (Modifier) -> Unit,
    subtitle: @Composable (Modifier) -> Unit,
    competitiveTierIcon: @Composable (Modifier) -> Unit
) {
    Row(
        modifier = Modifier
            .height(42.dp)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        agentPicture(
            Modifier
                .fillMaxHeight(1f)
                .aspectRatio(1f, true)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(2f, true)
        ) {
            if (titleAvailable) {
                Row(modifier = Modifier.fillMaxHeight(0.5f)) {
                    title(Modifier.align(Alignment.CenterVertically))
                }
            }
            Row(modifier = Modifier.fillMaxHeight(1f)) {
                subtitle(Modifier.align(Alignment.CenterVertically))
            }
        }
        competitiveTierIcon(
            Modifier
                .fillMaxHeight(1f)
                .aspectRatio(1f, true)
        )
    }
}

@Composable
private fun AgentPicture(
    modifier: Modifier,
    res: LocalImageData<*>?,
    key: Any,
    agentName: String,
) {
    val ctx = LocalContext.current
    val retryHash = remember {
        mutableStateOf(0)
    }.runRemember(key) {
        value++
    }
    val inspection = LocalInspectionMode.current
    AsyncImage(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(50))
            .border(1.dp, Color(0xFF60c18e), RoundedCornerShape(50)),
        model = remember(inspection, retryHash) {
            ImageRequest.Builder(ctx)
                .setParameter("retry_hash", retryHash, null)
                .run {
                    if (inspection && res is LocalImageData.Resource) placeholder(res.value) else this
                }
                .data(res?.value)
                .build()
        },
        contentDescription = "agent picture of ${agentName.ifEmpty { "not selected" }}",
    )
}

@Composable
private fun PlayerNameText(
    modifier: Modifier,
    gameName: String,
    tag: String,
    isUser: Boolean
) {
    val textColor =
        if (LocalIsThemeDark.current) Color.White else Color.Black
    val textStyle =
        MaterialTheme.typography.titleSmall
    val labelTextStyle =
        MaterialTheme.typography.labelMedium
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = gameName,
            color = textColor,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        // don't display on incognito
        if (tag.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .background(textColor.copy(alpha = 0.1f))
                    .padding(horizontal = 2.dp)
            ) {
                Text(text = "#$tag", color = textColor, style = labelTextStyle)
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .background(textColor.copy(alpha = 0.1f))
                    .padding(horizontal = 2.dp)
            ) {
                Text(text = "me", color = textColor, style = labelTextStyle)
            }
        }
    }
}


// TODO: might be better to show a lock icon instead
@Composable
private fun AgentIdentity(
    modifier: Modifier,
    agentName: String,
    roleName: String,
    roleIcon: LocalImageData<*>?,
    roleIconKey: Any,
    lockedIn: Boolean
) {
    val textColor =
        if (LocalIsThemeDark.current) Color.White else Color.Black
    val roleIconRetryHash = remember {
        mutableStateOf(0)
    }.runRemember(roleIconKey) {
        value++
    }
    val ctx = LocalContext.current
    val inspection = LocalInspectionMode.current
    Row(modifier = modifier) {
        // maybe add && if (roleIcon is LocalImageData.Resouce) rolceIcon.value != 0 else true
        if (roleName.isNotEmpty() && roleIcon != null) {
            AsyncImage(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.CenterVertically),
                model = remember(roleIconRetryHash) {
                    ImageRequest.Builder(ctx)
                        .run {
                            if (inspection && roleIcon is LocalImageData.Resource) {
                                placeholder(roleIcon.value)
                            } else {
                                data(roleIcon.value)
                            }
                        }
                        .build()
                },
                colorFilter = ColorFilter.tint(if (LocalIsThemeDark.current) Color.White else Color.Black),
                contentDescription = "$roleName role Icon"
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
            text = if (lockedIn) agentName else "picking ...",
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

}

@Composable
private fun CompetitiveTierIcon(
    modifier: Modifier,
    res: LocalImageData<*>?,
    key: Any,
    tierName: String
) {
    val ctx = LocalContext.current
    val retryHash = remember {
        mutableStateOf(0)
    }.runRemember(key) {
        value++
    }
    val inspection = LocalInspectionMode.current
    AsyncImage(
        modifier = modifier.fillMaxSize(),
        model = remember(key) {
            ImageRequest.Builder(ctx)
                .setParameter("retry_hash", retryHash, null)
                .run {
                    if (inspection && res is LocalImageData.Resource) placeholder(res.value) else this
                }
                .data(res?.value)
                .build()
        },
        contentDescription = "tier picture of $tierName",
    )
}