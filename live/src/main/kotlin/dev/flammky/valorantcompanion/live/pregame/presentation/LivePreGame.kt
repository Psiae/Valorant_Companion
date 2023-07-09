package dev.flammky.valorantcompanion.live.pregame.presentation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.assets.R as ASSET_R
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.map.LoadMapImageRequest
import dev.flammky.valorantcompanion.assets.map.ValorantMapImageType
import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.base.di.koin.getFromKoin
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.base.util.mutableValeContainerOf
import dev.flammky.valorantcompanion.live.pingStrengthInRangeOf4
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgent
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import dev.flammky.valorantcompanion.pvp.pregame.PreGameService
import dev.flammky.valorantcompanion.pvp.pregame.onSuccess
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.time.Duration

@Composable
fun LivePreGame(
    modifier: Modifier,
    isVisible: Boolean,
    state: LivePreGameUIState,
    dismiss: () -> Unit
) {

    BackHandler {
        dismiss()
    }

    if (!isVisible && state.isAutoRefreshOn) {
        state.eventSink(LivePreGameUIState.Event.SET_AUTO_REFRESH(false))
    } else if (isVisible && !state.isAutoRefreshOn) {
        state.eventSink(LivePreGameUIState.Event.SET_AUTO_REFRESH(true))
    }

    val unlockedAgents = rememberUnlockedAgents(
        puuid = state.user?.puuid ?: "",
        version = state.dataMod,
        versionContinuationKey = state.dataModContinuationKey
    )

    val disabledAgents = rememberDisabledAgents(
        players = state.ally?.players ?: persistentListOf()
    )

    LivePreGamePlacement(
        modifier.pointerInput(Unit) {},
        Background = {
            Background()
        },
        TopBarInfo = {
            TopBarInfo(
                modifier = Modifier,
                state
            )
         },
        AgentSelectionTeamMembersColumn = @Composable { placementModifier ->
            AgentSelectionColumn(
                modifier = placementModifier,
                state = rememberAgentSelectionPresenter().present(state)
            )
        },
        AgentSelectionLockInButton = @Composable { placementModifier ->
            AgentSelectionLockInButton(
                modifier = placementModifier,
                agentName = when {
                    state.user == null || state.user.characterID.isBlank() -> ""
                    else -> ValorantAgentIdentity.ofID(state.user.characterID)
                        ?.displayName
                        ?: "UNKNOWN_AGENT_NAME"
                },
                enabled = state.user != null &&
                        state.user.characterID.isNotEmpty() &&
                        state.user.characterSelectionState != CharacterSelectionState.LOCKED,
                onClick = {
                    state.eventSink(LivePreGameUIState.Event.LOCK_AGENT(state.user!!.characterID))
                }
            )
        },
        AgentSelectionPool = @Composable { placementModifier ->
            AgentSelectionPool(
                modifier = placementModifier,
                unlockedAgents = unlockedAgents,
                disabledAgents = disabledAgents,
                agentPool = ValorantAgent.asList(),
                selectedAgent = state.user?.characterID,
                onSelected = { uuid ->
                    state.user?.let { state.eventSink(LivePreGameUIState.Event.SELECT_AGENT(uuid)) }
                }
            )
        }
    )
}

@Composable
fun LivePreGame(
    modifier: Modifier,
    dismiss: () -> Unit
) {
    BackHandler(onBack = dismiss)
    val state = rememberLivePreGameScreenPresenter().present()
    LivePreGamePlacement(
        modifier,
        background = { backgroundModifier ->
            LivePreGameBackground(modifier = backgroundModifier)
        },
        content = { contentModifier ->
            LivePreGameContent(modifier = contentModifier, state = state)
        }
    )
}

@Composable
fun LivePreGamePlacement(
    modifier: Modifier,
    background: @Composable (Modifier) -> Unit,
    content: @Composable (Modifier) -> Unit
) = Box(
    modifier
        .fillMaxSize()
        .pointerInput(Unit) {}) {
    background(Modifier)
    content(Modifier)
}

@Composable
private fun rememberUnlockedAgents(
    preGameService: PreGameService = getFromKoin(),
    puuid: String,
    version: Long,
    versionContinuationKey: Any
): List<String> {
    if (puuid == "") return emptyList()
    val returns = remember(versionContinuationKey) {
        mutableStateOf<List<String>?>(null)
    }
    val polls = remember() {
        mutableStateOf<Any>(version)
    }.apply {
        value = remember(version, versionContinuationKey) { Any() }
    }
    val upContinuation = rememberUpdatedState(newValue = versionContinuationKey)
    val upReturns = rememberUpdatedState(newValue = returns)
    val client = remember(preGameService, puuid) {
        preGameService.createUserClient(puuid)
    }
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(
        key1 = client,
        effect = {
            val supervisor = SupervisorJob()
            var latestJob: Job? = null
            var currentPoll: Any? = null
            coroutineScope.launch(supervisor) {

                snapshotFlow { upContinuation }
                    .distinctUntilChanged()
                    .collect {
                        upReturns.value.value = null
                        latestJob?.cancel()
                        latestJob = launch {
                            while (true) {
                                val poll = polls.value
                                if (currentPoll == poll) break
                                val def = client
                                    .fetchUnlockedAgentsAsync()
                                coroutineContext.job.invokeOnCompletion { def.cancel() }
                                def.await()
                                    .onSuccess {
                                        upReturns.value.value = it.map { it.uuid }
                                    }
                                currentPoll = poll
                            }
                        }
                    }
            }

            onDispose { client.dispose() ; supervisor.cancel() }
        }
    )
    return returns.value ?: emptyList()
}

@Composable
private fun rememberDisabledAgents(
    players: ImmutableList<PreGamePlayer>
): List<String> {
    val source = remember {
        mutableValeContainerOf(players)
    }
    return remember {
        mutableStateOf(emptyList<String>(), neverEqualPolicy())
    }.apply {
        if (source.value === players) {
            return@apply
        }
        source.value = players
        val new = players
            .filter { it.characterSelectionState == CharacterSelectionState.LOCKED }
            .map { it.characterID }
        if (value != new) value = new
    }.value
}

@Composable
private fun currentUser(
    authRepository: RiotAuthRepository = getFromKoin()
): AuthenticatedAccount? {
    val activeAccountState = remember(authRepository) {
        mutableStateOf<AuthenticatedAccount?>(null)
    }
    DisposableEffect(
        authRepository,
    ) {
        val listener = ActiveAccountListener { old, new ->
            activeAccountState.value = new
        }
        authRepository.registerActiveAccountChangeListener(
            listener
        )
        onDispose {
            authRepository.unRegisterActiveAccountListener(listener)
        }
    }
    return activeAccountState.value
}

@Composable
private fun LivePreGamePlacement(
    modifier: Modifier,
    Background: @Composable () -> Unit,
    TopBarInfo: @Composable () -> Unit,
    AgentSelectionTeamMembersColumn: @Composable (Modifier) -> Unit,
    AgentSelectionLockInButton: @Composable (Modifier) -> Unit,
    AgentSelectionPool: @Composable (Modifier) -> Unit
) {
    Box {
        Background()
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 12.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp)
        ) {
            TopBarInfo()
            Spacer(modifier = Modifier.height(10.dp))
            AgentSelectionTeamMembersColumn(Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(10.dp))
            AgentSelectionLockInButton(Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(20.dp))
            AgentSelectionPool(Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun Background() = Box(
    modifier = Modifier
        .fillMaxSize()
        .background(Material3Theme.surfaceColorAsState().value),
)

@Composable
private fun TopBarInfo(
    modifier: Modifier = Modifier,
    state: LivePreGameUIState
) {
    val retryHash = remember(state.mapId) {
        mutableStateOf(0)
    }
    val mapImageErrorFlag = remember {
        mutableStateOf(false)
    }
    val mapModel = remember {
        mutableStateOf<ImageRequest?>(null)
    }.apply {
        value =
            presentMapModel(mapId = state.mapId, reloadKey = retryHash.value)
                .let { model ->
                    model.getOrElse {
                        mapImageErrorFlag.value = true
                        null
                    }
                }
    }
    Surface(
        modifier = modifier,
        color = Material3Theme.surfaceColorAsState().value
    ) {
        Column {
            if (mapImageErrorFlag.value) {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        text = "unexpected error while loading map image, please try again",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Icon(
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { retryHash.value++ },
                        painter = painterResource(id = ASSET_R.drawable.refresh_fill0_wght400_grad0_opsz48),
                        tint = Material3Theme.surfaceContentColorAsState().value,
                        contentDescription = "refresh image"
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    modifier = Modifier.matchParentSize(),
                    model = mapModel.value,
                    contentDescription = "Map ${state.mapName}",
                    contentScale = ContentScale.Crop,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f, false)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Material3Theme.surfaceVariantColorAsState().value.copy(alpha = 0.97f))
                            .padding(12.dp)
                    ) {
                        val textColor = if (LocalIsThemeDark.current) Color.White else Color.Black
                        Text(
                            text = "Map - ${state.mapName}".uppercase(),
                            color = textColor,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = state.gameModeName.uppercase(),
                            color = textColor,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row {
                            Text(
                                modifier = Modifier.weight(1f, false),
                                text = state.gamePodName.ifBlank { state.gamePodId },
                                color = textColor,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (state.gamePodPing > -1) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    modifier = Modifier.weight(1f, false),
                                    text = "(${state.gamePodPing}ms)",
                                    color = textColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(1.dp))
                                DrawLiveInGameTopBar4PingBar(modifier = Modifier
                                    .height(8.dp)
                                    .align(Alignment.CenterVertically), pingMs = 25)
                            }
                        }
                    }
                    run countdown@ {
                        val stepSec = state.countDown.inWholeSeconds
                        Log.d("LivePreGame.kt", "TopBarInfo_countDown@: stepSec=$stepSec")
                        if (stepSec < 0 || stepSec == Duration.INFINITE.inWholeSeconds) return@countdown
                        Spacer(modifier = Modifier.width(12.dp))
                        BoxWithConstraints(
                            modifier = Modifier
                                .size(68.dp)
                                .align(Alignment.CenterVertically)
                                .clip(CircleShape)
                                .background(
                                    Material3Theme.surfaceVariantColorAsState().value.copy(
                                        alpha = 0.97f
                                    )
                                )
                        ) {

                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .align(Alignment.Center),
                                progress = stepSec.toFloat() / 85,
                                strokeWidth = 2.dp,
                                color = if (stepSec <= 10f) Color.Red else Color.Green
                            )
                            val text = stepSec.toInt().toString()
                            Text(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(6.dp),
                                text = text,
                                textAlign = TextAlign.Center,
                                color = if (LocalIsThemeDark.current) Color.White else Color.Black,
                                style = MaterialTheme.typography.labelLarge
                                    .copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize =  when (text.length) {
                                            1 -> 43.sp
                                            else -> ((68.dp - 6.dp - 2.dp) / text.length).value.sp
                                        }.also {
                                            Log.d(
                                                "LivePreGame.kt",
                                                "TopBarInfo_countDown@: fontSize=$it")
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun presentMapModel(
    mapId: String,
    reloadKey: Any,
): Result<ImageRequest> {
    val ctx = LocalContext.current

    val returns = remember(mapId) {
        mutableStateOf(
            Result.success(ImageRequest.Builder(ctx).build())
        )
    }

    val retryHashState = remember(mapId) {
        mutableStateOf<Any>(0)
    }.apply {
        value = reloadKey
    }

    val assetService = getFromKoin<ValorantAssetsService>()
    val assetClient = remember(assetService) {
        assetService.createLoaderClient()
    }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(
        key1 = assetClient,
        effect = {
            onDispose { assetClient.dispose() }
        }
    )
    DisposableEffect(
        key1 = assetClient,
        key2 = mapId,
        key3 = retryHashState.value,
        effect = {
            val supervisor = SupervisorJob()

            coroutineScope.launch(supervisor) {
                assetClient
                    .loadMapImageAsync(
                        LoadMapImageRequest {
                            uuid(mapId)
                            acceptableTypes(
                                ValorantMapImageType.ListView,
                                ValorantMapImageType.Splash
                            )
                        }
                    )
                    .await()
                    .onSuccess { file ->
                        returns.value = Result.success(
                            ImageRequest
                                .Builder(ctx)
                                .setParameter("retry_hash", retryHashState.value)
                                .data(file)
                                .build()
                        )
                    }
                    .onFailure {
                        returns.value = Result.failure(it)
                    }
            }

            // TODO: File watcher

            onDispose { supervisor.cancel() }
        }
    )


    return returns.value
}

@Composable
private fun DrawLiveInGameTopBar4PingBar(
    modifier: Modifier,
    pingMs: Int
) {
    val strength = pingStrengthInRangeOf4(pingMs)
    check(strength in 1..4)
    val color = when (strength) {
        1 -> Color.Red
        2 -> Color.Yellow
        3, 4 -> Color.Green
        else -> error("Unguarded condition")
    }
    BoxWithConstraints(
        modifier = modifier.size(24.dp)
    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(4) { i ->
                val n = i + 1
                val height = (maxHeight.value * (n.toFloat() / 4)).dp
                val width = maxHeight / 4
                Box(
                    modifier = Modifier
                        .height(height)
                        .width(width)
                        .background(color.takeIf { strength >= n } ?: Color.Gray)
                        .shadow(2.dp)
                )
            }
        }
    }
}