package dev.flammky.valorantcompanion.boarding.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotLoginRequest
import kotlinx.coroutines.*

@Composable
fun rememberLoginScreenPresenter(
    riotAuthService: RiotAuthService = org.koin.androidx.compose.get()
): LoginScreenPresenter {
    return remember() {
        LoginScreenPresenter(riotAuthService)
    }
}

class LoginScreenPresenter(
    private val riotAuthService: RiotAuthService
) {

    private val coroutineDispatchScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    @OptIn(ExperimentalCoroutinesApi::class)
    @Composable
    fun present(): LoginScreenState {
        return remember {
            LoginScreenState(
                intents = LoginScreenIntents(
                    loginRiotID = { username, password, retain ->
                        coroutineDispatchScope.async(SupervisorJob()) {
                            val login = riotAuthService.createLoginClient().login(
                                RiotLoginRequest(username, password),
                                true
                            )
                            suspendCancellableCoroutine { ucont ->
                                login.invokeOnCompletion {
                                    ucont.resume(Unit) {}
                                }
                            }
                            login.ex?.let {
                                it.printStackTrace()
                                throw it
                            }
                        }
                    }
                )
            )
        }
    }
}