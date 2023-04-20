package dev.flammky.valorantcompanion.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository

@Composable
fun rememberRootNavigationPresenter(): RootNavigationPresenter {
    val authRepo = org.koin.androidx.compose.get<RiotAuthRepository>()
    return remember {
        RootNavigationPresenter(authRepo)
    }
}


class RootNavigationPresenter(
    private val authRepository: RiotAuthRepository
) {

    @Composable
    fun present(): RootNavigationState {
        val state = remember { RootNavigationState() }

        PresentBoardingState(navigationState = state)

        return state
    }

    @Composable
    private fun PresentBoardingState(
        navigationState: RootNavigationState,
    ) {
        val handle = remember {
            ActiveAccountListener { old, new ->
                navigationState.showBoarding = new == null
            }
        }
        DisposableEffect(
            key1 = handle,
            effect = {
                authRepository.registerActiveAccountChangeListener(handle)
                onDispose { authRepository.unRegisterActiveAccountListener(handle) }
            }
        )
    }
}