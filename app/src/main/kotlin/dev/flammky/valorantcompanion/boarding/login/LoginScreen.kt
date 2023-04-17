package dev.flammky.valorantcompanion.boarding.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun LoginScreen(
    state: LoginScreenState
) {
    LoginScreenPlacement(
        form = { modifier ->
            LoginForm(
                modifier = modifier,
                state = rememberLoginFormPresenter().present(screenState = state)
            )
        },
        loading = { modifier ->
            LoginLoadingScreen(
                modifier = modifier,
                show = state.showLoading
            )
        }
    )
}

@Composable
private fun LoginScreenPlacement(
    form: @Composable (Modifier) -> Unit,
    loading: @Composable (Modifier) -> Unit,
) = Box(modifier = Modifier.fillMaxSize()) {
    form(Modifier.fillMaxSize())
    loading(Modifier.fillMaxSize())
}