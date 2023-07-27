package dev.flammky.valorantcompanion.live.pvp.ingame.presentation

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.surfaceColorAsState
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.compose.composeWithKey
import dev.flammky.valorantcompanion.base.compose.rememberWithCompositionObserver
import dev.flammky.valorantcompanion.base.compose.state.subCompose
import dev.flammky.valorantcompanion.base.theme.material3.surfaceVariantColorAsState
import dev.flammky.valorantcompanion.base.util.mutableValueContainerOf
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job

@Composable
internal fun LiveInGameTeamMemberCard(
    modifier: Modifier,
    state: LiveInGameTeamMemberCardState
) = LiveInGameTeamMemberCardPlacement(
    modifier = modifier,
    surface = {
        Box(
            it
                .fillMaxSize()
                .background(Material3Theme.surfaceColorAsState().value))
    },
    agentPicture = {
        LiveinGameTeamMemberCardAgentPicture(
            modifier = it,
            key = state.agentIconKey,
            res = state.agentIcon
        )
    },
    title = {
        LiveInGameTeamMemberCardTitle(
            modifier = it,
            username = state.username,
            tagline = state.tagline,
            isUser = state.isUser
        )
    },
    subtitle = {
        LiveInGameTeamMemberCardSubtitle(
            modifier = it,
            agentName = state.agentName,
            roleName = state.roleName,
            roleIcon = state.roleIcon,
            roleIconKey = state.roleIconKey
        )
    },
    competitiveTierIcon = {
        LiveInGameTeamMemberCardCompetitiveTierIcon(
            modifier = it,
            res = state.competitiveTierIcon,
            resKey = state.competitiveTierIconKey
        )
    },
    errorMessages = {
        if (state.errorCount > 0) {
            LiveInGameTeamMemberCardErrorMessages(
                modifier = it,
                messages = state.getErrors()
            )
        }
    }
)

@Composable
private fun LiveInGameTeamMemberCardPlacement(
    modifier: Modifier,
    surface: @Composable (Modifier) -> Unit,
    agentPicture: @Composable (Modifier) -> Unit,
    title: @Composable (Modifier) -> Unit,
    subtitle: @Composable (Modifier) -> Unit,
    competitiveTierIcon: @Composable (Modifier) -> Unit,
    errorMessages: @Composable (Modifier) -> Unit
) = Box(modifier = modifier) {
    surface(Modifier.matchParentSize())
    Column {
        Row(Modifier.padding(horizontal = 4.dp, vertical = 2.dp).height(42.dp)) {
            agentPicture(Modifier.align(Alignment.CenterVertically))
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(2f, true)
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    title(Modifier.align(Alignment.CenterVertically))
                }
                Row(modifier = Modifier.weight(1f)) {
                    subtitle(Modifier.align(Alignment.CenterVertically))
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            competitiveTierIcon(
                Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f, true)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Box { errorMessages(Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp)) }
    }
}

@Composable
private fun LiveinGameTeamMemberCardAgentPicture(
    modifier: Modifier,
    key: Any,
    res: LocalImage<*>?
) {
    val ctx = LocalContext.current
    AsyncImage(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f, true)
            .clip(RoundedCornerShape(50))
            .border(1.dp, Color(0xFF60c18e), RoundedCornerShape(50)),
        model = remember(key) {
            ImageRequest.Builder(ctx)
                .setParameter("key", key, null)
                .data(res?.value)
                .build()
        },
        contentDescription = null
    )
}

@Composable
private fun LiveInGameTeamMemberCardTitle(
    modifier: Modifier,
    username: String?,
    tagline: String?,
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

        if (username != null) {
            Text(
                text = username,
                color = textColor,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // should we hide this when incognito is enabled ?
        if (tagline != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .background(textColor.copy(alpha = 0.1f))
                    .padding(horizontal = 2.dp)
            ) {
                Text(text = "#$tagline", color = textColor, style = labelTextStyle)
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

@Composable
private fun LiveInGameTeamMemberCardSubtitle(
    modifier: Modifier,
    agentName: String?,
    roleName: String?,
    roleIcon: LocalImage<*>?,
    roleIconKey: Any,
) {
    val ctx = LocalContext.current
    val textColor =
        if (LocalIsThemeDark.current) Color.White else Color.Black
    Row(modifier = modifier) {
        // maybe add && if (roleIcon is LocalImageData.Resouce) rolceIcon.value != 0 else true
        if (roleIcon != null) {
            AsyncImage(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.CenterVertically),
                model = remember(roleIconKey) {
                    ImageRequest.Builder(ctx)
                        .run {
                            data(roleIcon.value)
                        }
                        .build()
                },
                colorFilter = ColorFilter.tint(if (LocalIsThemeDark.current) Color.White else Color.Black),
                contentDescription = roleName?.let { "$roleName role Icon" }
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        if (agentName != null) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically),
                text = agentName,
                color = textColor,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LiveInGameTeamMemberCardCompetitiveTierIcon(
    modifier: Modifier,
    res: LocalImage<*>?,
    resKey: Any
) {
    val ctx = LocalContext.current
    AsyncImage(
        modifier = modifier
            .fillMaxSize(),
        model = remember(resKey) {
            ImageRequest.Builder(ctx)
                .setParameter("key", resKey, null)
                .data(res?.value)
                .build()
        },
        contentDescription = null
    )
}

@Composable
private fun LiveInGameTeamMemberCardErrorMessages(
    modifier: Modifier,
    messages: List<LiveInGameTeamMemberCardErrorMessage>
) = Column(modifier.heightIn(min = 32.dp)) {
    messages.forEachIndexed { i, message ->
        LiveInGameTeamMemberCardErrorMessageUI(modifier = Modifier, message = message)
        if (i != messages.lastIndex) Spacer(modifier = Modifier.height(5.dp))
    }
}

@Composable
private fun LiveInGameTeamMemberCardErrorMessageUI(
    modifier: Modifier,
    message: LiveInGameTeamMemberCardErrorMessage
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
                                rememberWithCompositionObserver(
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
                            painter = painterResource(id = R_ASSET_DRAWABLE.refresh_fill0_wght400_grad0_opsz48),
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