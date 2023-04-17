package dev.flammky.valorantcompanion.boarding.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import dev.flammky.valorantcompanion.auth.RiotAuthService
import kotlinx.coroutines.CoroutineScope
import org.koin.androidx.compose.get

@Composable
fun rememberLoginFormPresenter(): LoginFormPresenter {
    return remember() {
        LoginFormPresenter()
    }
}

class LoginFormPresenter() {

    @Composable
    fun present(
        screenState: LoginScreenState
    ): LoginFormState {
        val coroutineScope = rememberCoroutineScope()
        val loginFormIntents = remember(screenState) { loginFormIntents(screenState, coroutineScope) }
        return rememberSaveable(
            this, screenState,
            saver = LoginFormState.Saver(loginFormIntents)
        ) {
            LoginFormState(loginFormIntents)
        }
    }

    fun loginFormIntents(
        screenState: LoginScreenState,
        coroutineScope: CoroutineScope
    ) = LoginFormIntents(
        login = { self, username: String, password: String, retain: Boolean ->
            val def = screenState.intents.loginRiotID(username, password, retain)
            screenState.onLogin(def)
            def.invokeOnCompletion {
                self.resetSlotPasswordWithExceptionMessage("Your username or password may be incorrect")
            }
        }
    )
}

class MockedLoginFormPresenter()