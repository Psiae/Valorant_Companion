package dev.flammky.valorantcompanion.career.main

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.auth.riot.RiotAuthenticatedAccount
import dev.flammky.valorantcompanion.career.R
import org.koin.androidx.compose.get as getFromKoin

@Composable
fun rememberPlayerProfileCardPresenter(
    repository: RiotAuthRepository = getFromKoin()
): PlayerProfileCardPresenter {
    return remember { PlayerProfileCardPresenter(repository) }
}

class PlayerProfileCardPresenter(
    private val repository: RiotAuthRepository
) {

    @Composable
    fun present(
        account: RiotAuthenticatedAccount? = null
    ): PlayerProfileCardState {
        val state = remember(this) { PlayerProfileCardState() }

        val acc = account ?: observeActiveAccount()

        remember(acc) {
            state.apply {
                profilePicture = R.drawable.wishing_you_happiness
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

        return state
    }

    @Composable
    private fun observeActiveAccount(): RiotAuthenticatedAccount? {
        val returns = remember {
            mutableStateOf<RiotAuthenticatedAccount?>(repository.activeAccount as RiotAuthenticatedAccount?)
        }
        DisposableEffect(
            key1 = this,
            effect = {
                val listener = ActiveAccountListener { old, new ->
                    returns.value = new as RiotAuthenticatedAccount?
                }
                repository.registerActiveAccountChangeListener(listener)
                onDispose { repository.unRegisterActiveAccountListener(listener) }
            }
        )
        return returns.value
    }
}