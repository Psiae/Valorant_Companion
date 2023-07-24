package dev.flammky.valorantcompanion.live.pregame.presentation

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.base.rememberThis
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.compose.composeWithKey
import dev.flammky.valorantcompanion.base.compose.rememberEffect
import dev.flammky.valorantcompanion.base.compose.state.subCompose
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.surfaceVariantColorAsState
import dev.flammky.valorantcompanion.base.util.mutableValueContainerOf
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job

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
            res = state.competitiveTierIcon,
            key = state.competitiveTierIconKey,
            tierName = state.competitiveTierName
        )
    },
    errorMessages = { modifier ->
        if (state.errorCount > 0) {
            AgentSelectionCardErrorMessages(modifier = modifier, messages = state.getErrors())
        }
    }
)

@Composable
private fun AgentSelectionPlayerCardPlacement(
    titleAvailable: Boolean,
    agentPicture: @Composable (Modifier) -> Unit,
    title: @Composable (Modifier) -> Unit,
    subtitle: @Composable (Modifier) -> Unit,
    competitiveTierIcon: @Composable (Modifier) -> Unit,
    errorMessages: @Composable (Modifier) -> Unit
) {
    Column {
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
            Spacer(modifier = Modifier.width(8.dp))
            competitiveTierIcon(
                Modifier
                    .fillMaxHeight(1f)
                    .aspectRatio(1f, true)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Box { errorMessages(Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp)) }
    }
}

@Composable
private fun AgentPicture(
    modifier: Modifier,
    res: LocalImage<*>?,
    key: Any,
    agentName: String,
) {
    val ctx = LocalContext.current
    val retryHash = remember {
        mutableStateOf(0)
    }.rememberThis(key) {
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
                    if (inspection && res is LocalImage.Resource) placeholder(res.value) else this
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
    roleIcon: LocalImage<*>?,
    roleIconKey: Any,
    lockedIn: Boolean
) {
    val textColor =
        if (LocalIsThemeDark.current) Color.White else Color.Black
    val roleIconRetryHash = remember {
        mutableStateOf(0)
    }.rememberThis(roleIconKey) {
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
                            if (inspection && roleIcon is LocalImage.Resource) {
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
    res: LocalImage<*>?,
    key: Any,
    tierName: String
) {
    val ctx = LocalContext.current
    val retryHash = remember {
        mutableStateOf(0)
    }.rememberThis(key) {
        value++
    }
    val inspection = LocalInspectionMode.current
    AsyncImage(
        modifier = modifier.fillMaxSize(),
        model = remember(key) {
            ImageRequest.Builder(ctx)
                .setParameter("retry_hash", retryHash, null)
                .run {
                    if (inspection && res is LocalImage.Resource) placeholder(res.value) else this
                }
                .data(res?.value)
                .build()
        },
        contentDescription = "tier picture of $tierName",
    )
}

@Composable
private fun AgentSelectionCardErrorMessages(
    modifier: Modifier,
    messages: List<AgentSelectionPlayerCardErrorMessage>
) = Column(modifier.heightIn(min = 32.dp)) {
    messages.forEachIndexed { i, message ->
        AgentSelectionCardErrorMessageUI(modifier = Modifier, message = message)
        if (i != messages.lastIndex) Spacer(modifier = Modifier.height(5.dp))
    }
}

@Composable
private fun AgentSelectionCardErrorMessageUI(
    modifier: Modifier,
    message: AgentSelectionPlayerCardErrorMessage
) {
    val contentColor =
        if (LocalIsThemeDark.current) Color.White
        else Color.Black
    Column(modifier) {
        composeWithKey(message.component, contentColor) { componentName, cc ->
            BasicText(
                modifier = Modifier,
                style = MaterialTheme3.typography.labelSmall.copy(color = cc),
                text = componentName,
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        subCompose {
            val refreshing = remember(message.refresh) { mutableStateOf(false) }
            Row(
                modifier = remember {
                    Modifier.heightIn(min = 26.dp)
                } then remember(refreshing.value) {
                    if (refreshing.value)
                        Modifier.alpha(0.38f)
                    else
                        Modifier
                } then remember(message.refresh) {
                    Modifier.composed(
                        factory = {
                            if (message.refresh != null) {
                                val def = remember { mutableValueContainerOf<Job?>(null) }
                                val handle = remember { mutableValueContainerOf<DisposableHandle?>(null) }
                                rememberEffect(
                                    message.refresh,
                                    onRemembered = {},
                                    onForgotten = { handle.value?.dispose() ; def.value?.cancel() },
                                    onAbandoned = { handle.value?.dispose() ; def.value?.cancel() }
                                ) {
                                    Modifier.clickable {
                                        message.refresh.invoke()?.let { session ->
                                            def.value = session
                                            refreshing.value = !session.isCompleted
                                            handle.value = session.invokeOnCompletion { refreshing.value = false }
                                        }
                                    }
                                }
                            } else {
                                Modifier
                            }
                        }
                    )
                }
            ) {
                composeWithKey(message.message) { rMessage ->
                    BasicText(
                        modifier = Modifier
                            .weight(1f, true)
                            .align(Alignment.CenterVertically),
                        style = MaterialTheme3.typography.labelSmall.copy(color = MaterialTheme3.colorScheme.error),
                        text = rMessage,
                    )
                }
                composeWithKey(message.refresh, contentColor) { rMessageRefresh, rContentColor ->
                    if (rMessageRefresh != null) {
                        Icon(
                            modifier = Modifier
                                .size(26.dp)
                                .align(Alignment.CenterVertically),
                            painter = painterResource(
                                id = R_ASSET_DRAWABLE.refresh_fill0_wght400_grad0_opsz48
                            ),
                            tint = rContentColor,
                            contentDescription = "try again"
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = Material3Theme.surfaceVariantColorAsState().value
        )
    }
}