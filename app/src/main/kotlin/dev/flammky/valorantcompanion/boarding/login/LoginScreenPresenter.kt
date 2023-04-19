package dev.flammky.valorantcompanion.boarding.login

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotLoginRequest
import kotlinx.coroutines.*
import kotlin.math.log

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
                            val login = riotAuthService.loginAsync(
                                RiotLoginRequest(username, password)
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
                            login.userInfo.data != null
                        }
                    }
                )
            )
        }
    }
}