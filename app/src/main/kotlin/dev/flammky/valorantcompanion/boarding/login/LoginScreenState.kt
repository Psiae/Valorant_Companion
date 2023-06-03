package dev.flammky.valorantcompanion.boarding.login

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*

data class LoginScreenIntents(
    val loginRiotID: (
        username: String,
        password: String,
        retain: Boolean,
    ) -> Deferred<*>,
) {
    companion object {
        fun mock(coroutineScope: CoroutineScope, timeMs: Long): LoginScreenIntents = LoginScreenIntents(
            loginRiotID = { username, password, retain ->
                coroutineScope.async {
                    runCatching { delay(timeMs) ; false }
                }
            }
        )
    }
}

class LoginScreenState(
    val intents: LoginScreenIntents
) {

    var loginInstance by mutableStateOf<LoginInstanceState?>(null)

    val showLoading by derivedStateOf { loginInstance?.loading == true }

    fun onLogin(
        def: Deferred<*>
    ) {
        if (loginInstance?.isCompleted() != true) {
            loginInstance = LoginInstanceState(def)
        }
    }
}

class LoginInstanceState(
    private val def: Deferred<*>
) {

    var loading by mutableStateOf(!def.isCompleted)

    fun isCompleted() = def.isCompleted

    init {
        def.invokeOnCompletion { loading = false }
    }
}