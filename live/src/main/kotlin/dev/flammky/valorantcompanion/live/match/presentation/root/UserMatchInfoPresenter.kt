package dev.flammky.valorantcompanion.live.match.presentation.root

import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.base.compose.SnapshotRead
import dev.flammky.valorantcompanion.live.pregame.presentation.LivePreGameUIState
import dev.flammky.valorantcompanion.pvp.pregame.PreGameClient
import dev.flammky.valorantcompanion.pvp.pregame.PreGameService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class UserMatchInfoPresenter(
    private val pregameService: PreGameService,
    private val authRepository: RiotAuthRepository,
) {

    @Composable
    fun present(): UserMatchInfoUIState {
        val activeAccountState = remember(this) {
            mutableStateOf<AuthenticatedAccount?>(null)
        }
        DisposableEffect(
            this,
        ) {
            val listener = ActiveAccountListener { old, new ->
                activeAccountState.value = new
            }
            authRepository.registerActiveAccountChangeListener(
                listener
            )
            onDispose {
                authRepository.unRegisterActiveAccountListener(listener)
            }
        }
        return present(puuid = activeAccountState.value?.model?.id ?: "")
    }

    @Composable
    private fun present(
        puuid: String
    ): UserMatchInfoUIState {
        if (puuid.isBlank()) {
            return UserMatchInfoUIState.UNSET
        }
        return remember(this, puuid) { UserMatchInfoUIStateProducer(puuid) }
            .apply { onRemembered() }
            .produceState()
    }


    private inner class UserMatchInfoUIStateProducer(
        private val puuid: String
    ) : RememberObserver {

        private val state = mutableStateOf(UserMatchInfoUIState.UNSET)
        private var rememberedByComposition = false
        private var forgottenByComposition = false
        private var init = false
        // lateinit
        private var coroutineScope: CoroutineScope? = null
        // lateinit
        private var preGameClient: PreGameClient? = null

        @SnapshotRead
        fun produceState(): UserMatchInfoUIState {
            check(rememberedByComposition) {
                "produceState is called before onRemembered"
            }
            check(!forgottenByComposition) {
                "produceState is called after onForgotten"
            }
            if (init) {
                initialize()
            }
            return state.value
        }

        override fun onAbandoned() {
            dispose()
        }

        override fun onForgotten() {
            dispose()
        }

        override fun onRemembered() {
            if (rememberedByComposition) return
            rememberedByComposition = true
            coroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
        }

        private fun dispose() {
            coroutineScope?.cancel()
            preGameClient?.dispose()
        }

        private fun initialize() {
            check(init)
            init = false
            preGameClient = pregameService.createClient(puuid)
        }

        private fun initialState(): UserMatchInfoUIState {
            return UserMatchInfoUIState
                .UNSET
                .copy(

                )
        }
    }
}



@Composable
fun rememberUserMatchInfoPresenter(
    pregameService: PreGameService,
    authRepository: RiotAuthRepository
): UserMatchInfoPresenter {
    return remember(pregameService, authRepository) {
        UserMatchInfoPresenter(
            pregameService = pregameService,
            authRepository = authRepository
        )
    }
}