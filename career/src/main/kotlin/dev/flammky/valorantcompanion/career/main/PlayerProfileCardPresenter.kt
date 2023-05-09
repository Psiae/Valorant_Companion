package dev.flammky.valorantcompanion.career.main

import android.util.Log
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.assets.PlayerCardArtType
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.LoadPlayerCardRequest
import dev.flammky.valorantcompanion.auth.riot.*
import dev.flammky.valorantcompanion.auth.riot.region.GeoShardInfo
import dev.flammky.valorantcompanion.auth.riot.region.RiotRegion
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadoutService
import kotlinx.coroutines.*
import org.koin.androidx.compose.get as getFromKoin

@Composable
fun rememberPlayerProfileCardPresenter(
    authRepo: RiotAuthRepository = getFromKoin(),
    geoRepo: RiotGeoRepository = getFromKoin(),
    assetsService: ValorantAssetsService = getFromKoin(),
    loadoutService: PlayerLoadoutService = getFromKoin()
): PlayerProfileCardPresenter {
    return remember { PlayerProfileCardPresenter(authRepo, geoRepo, assetsService, loadoutService) }
}

class PlayerProfileCardPresenter(
    private val authRepo: RiotAuthRepository,
    private val geoRepo: RiotGeoRepository,
    private val assetsService: ValorantAssetsService,
    private val loadoutService: PlayerLoadoutService
) {

    @Composable
    fun present(
        account: RiotAuthenticatedAccount? = null
    ): PlayerProfileCardState {
        val state = remember(this) { PlayerProfileCardState() }

        val acc = account ?: observeActiveAccount()
        val picture = observePlayerArtCard(acc?.model?.puuid ?: "")
        val geo = observePlayerGeo(puuid = acc?.model?.puuid ?: "")

        remember(acc) {
            state.apply {
                riotId = acc?.model?.game_name?.let { str ->
                    if (str.startsWith("\"") && str.endsWith("\""))
                        str.substring(1, str.lastIndex)
                    else
                        str
                }
                tagLine = acc?.model?.tagline?.let { str ->
                    if (str.startsWith("\"") && str.endsWith("\""))
                        str.substring(1, str.lastIndex)
                    else
                        str
                }
                Log.d("PlayerProfileCardPresenter", "newAccount($riotId, $tagLine)")
            }
        }
        remember(picture) {
            state.apply {
                profilePicture = picture
            }
        }
        remember(geo) {
            state.apply {
                region = when(geo?.region) {
                    RiotRegion.APAC -> "APAC"
                    RiotRegion.BR -> "BR"
                    RiotRegion.EU -> "EU"
                    RiotRegion.KR -> "KR"
                    RiotRegion.LATAM -> "LATAM"
                    RiotRegion.NA -> "NA"
                    null -> null
                }
            }
        }

        return state
    }

    @Composable
    private fun observeActiveAccount(): RiotAuthenticatedAccount? {
        val returns = remember {
            mutableStateOf<RiotAuthenticatedAccount?>(authRepo.activeAccount as RiotAuthenticatedAccount?)
        }
        DisposableEffect(
            key1 = this,
            effect = {
                val listener = ActiveAccountListener { old, new ->
                    returns.value = new as RiotAuthenticatedAccount?
                }
                authRepo.registerActiveAccountChangeListener(listener)
                onDispose { authRepo.unRegisterActiveAccountListener(listener) }
            }
        )
        return returns.value
    }

    @Composable
    private fun observePlayerArtCard(
        puuid: String
    ): Any? {
        val upPuuid = rememberUpdatedState(puuid)
        val assetLoader = remember(this) {
            assetsService.createLoaderClient()
        }
        val loadoutClient = remember(this) {
            loadoutService.createClient()
        }
        val returns = remember {
            mutableStateOf<Any?>(null)
        }
        val coroutineScope = rememberCoroutineScope()
        DisposableEffect(
            this
        ) {
            val supervisor = SupervisorJob()
            coroutineScope.launch(supervisor) {
                var latestJob: Job? = null
                snapshotFlow { upPuuid.value }.collect { puuid ->
                    latestJob?.cancel()
                    latestJob = launch {
                        runCatching {
                            val cardId = loadoutClient
                                .getCachedOrFetchPlayerLoadoutAsync(puuid)
                                .await()
                                .onFailure { it.printStackTrace() }
                                .getOrNull()?.identity?.playerCardId
                                ?: return@launch
                            assetLoader.loadUserPlayerCardAsync(
                                LoadPlayerCardRequest(
                                    cardId,
                                    PlayerCardArtType.SMALL, PlayerCardArtType.TALL
                                )
                            ).await()
                        }.onFailure {
                            it.printStackTrace()
                            if (currentCoroutineContext().job.isActive) returns.value = null
                        }.onSuccess {
                            if (currentCoroutineContext().job.isActive) returns.value = it
                        }
                    }
                }
            }
            onDispose {
                supervisor.cancel()
                assetLoader.dispose()
                loadoutClient.dispose()
            }
        }
        return returns.value
    }

    @Composable
    private fun observePlayerGeo(
        puuid: String
    ): GeoShardInfo? {
        val returns = remember(this, puuid) {
            mutableStateOf<GeoShardInfo?>(null)
        }
        val coroutineScope = rememberCoroutineScope()
        DisposableEffect(
            key1 = returns,
            effect = {
                val supervisor = SupervisorJob()

                val listener = UserGeoShardInfoListener(
                    puuid,
                    initial = returns::value::set,
                    onRemoteInfoChanged = returns::value::set,
                    onUserOverriding = { }
                )

                coroutineScope.launch(supervisor) {
                    geoRepo.registerUserGeoShardInfoChangeListener(listener)
                }

                onDispose { supervisor.cancel() ; geoRepo.unregisterUserGeoShardInfoChangeListener(listener) }
            }
        )
        return returns.value
    }
}