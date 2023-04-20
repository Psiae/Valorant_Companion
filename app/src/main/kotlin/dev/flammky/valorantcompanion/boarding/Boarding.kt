package dev.flammky.valorantcompanion.boarding

import androidx.compose.runtime.Composable
import dev.flammky.valorantcompanion.boarding.login.LoginScreen
import dev.flammky.valorantcompanion.boarding.login.rememberLoginScreenPresenter

@Composable
fun RootBoarding(
    state: MainBoardingState
) {
    LoginScreen(state = rememberLoginScreenPresenter().present())
}