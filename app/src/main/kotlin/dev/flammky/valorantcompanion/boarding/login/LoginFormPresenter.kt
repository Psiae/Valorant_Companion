package dev.flammky.valorantcompanion.boarding.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import dev.flammky.valorantcompanion.auth.ex.AuthFailureException
import kotlinx.coroutines.CoroutineScope

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
            def.invokeOnCompletion { ex ->
                self.resetSlotPasswordWithExceptionMessage(
                    ex
                        ?.let {
                            it.printStackTrace()
                            if (ex is AuthFailureException) {
                                "Your username or password may be incorrect"
                            } else {
                                "Unexpected Error: $ex"
                            }
                        }
                        ?: ""
                )
            }
        }
    )
}

class MockedLoginFormPresenter()