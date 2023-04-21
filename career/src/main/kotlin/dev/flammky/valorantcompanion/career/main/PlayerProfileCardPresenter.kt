package dev.flammky.valorantcompanion.career.main

import android.util.Log
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.assets.PlayerCardArtType
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.internal.LoadPlayerCardRequest
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.auth.riot.RiotAuthenticatedAccount
import dev.flammky.valorantcompanion.career.R
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get as getFromKoin

@Composable
fun rememberPlayerProfileCardPresenter(
    authRepo: RiotAuthRepository = getFromKoin(),
    assetsService: ValorantAssetsService = getFromKoin()
): PlayerProfileCardPresenter {
    return remember { PlayerProfileCardPresenter(authRepo, assetsService) }
}

class PlayerProfileCardPresenter(
    private val authRepo: RiotAuthRepository,
    private val assetsService: ValorantAssetsService
) {

    @Composable
    fun present(
        account: RiotAuthenticatedAccount? = null
    ): PlayerProfileCardState {
        val state = remember(this) { PlayerProfileCardState() }

        val acc = account ?: observeActiveAccount()
        val picture = observePlayerArtCard()


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

    ): Any? {
        val assetLoader = remember(this) {
            assetsService.createLoaderClient()
        }
        val returns = remember {
            mutableStateOf<Any?>(null)
        }
        val coroutineScope = rememberCoroutineScope()
        DisposableEffect(
            key1 = assetLoader,
            effect = {
                val supervisor = SupervisorJob()
                coroutineScope.launch(supervisor) {
                    runCatching {
                        assetLoader.loadUserPlayerCardAsync(
                            LoadPlayerCardRequest(
                                "72f9b137-41f1-ef95-853a-798212f064ee",
                                PlayerCardArtType.SMALL, PlayerCardArtType.TALL
                            )
                        ).await()
                    }.onFailure {
                        it.printStackTrace()
                        if (supervisor.isActive) returns.value = null
                    }.onSuccess {
                        if (supervisor.isActive) returns.value = it
                    }
                }
                onDispose {
                    supervisor.cancel()
                    assetLoader.dispose()
                }
            }
        )
        return returns.value
    }
}