package dev.flammky.valorantcompanion.boarding.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

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

    @Composable
    fun present(): LoginScreenState {
        return remember {
            LoginScreenState(
                intents = LoginScreenIntents.mock(coroutineDispatchScope, 1000)
            )
        }
    }
}