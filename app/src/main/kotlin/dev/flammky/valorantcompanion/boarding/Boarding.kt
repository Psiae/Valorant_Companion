package dev.flammky.valorantcompanion.boarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.flammky.valorantcompanion.boarding.login.LoginScreen
import dev.flammky.valorantcompanion.boarding.login.LoginScreenIntents
import dev.flammky.valorantcompanion.boarding.login.LoginScreenState
import dev.flammky.valorantcompanion.boarding.login.rememberLoginScreenPresenter

@Composable
fun MainBoarding(
    state: MainBoardingState
) {
    LoginScreen(state = rememberLoginScreenPresenter().present())
}