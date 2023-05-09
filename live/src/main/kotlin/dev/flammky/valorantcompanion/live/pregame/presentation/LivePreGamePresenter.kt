package dev.flammky.valorantcompanion.live.pregame.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.flammky.valorantcompanion.pvp.pregame.PreGameService
import org.koin.androidx.compose.get as getFromKoin

@Composable
fun rememberLivePreGamePresenter(
    pregameService: PreGameService = getFromKoin()
): LivePreGamePresenter {
    return remember(pregameService) {
        LivePreGamePresenter(pregameService)
    }
}

class LivePreGamePresenter(
    private val pregameService: PreGameService
) {


    @Composable
    fun present(): LivePreGameState {
       val state =  remember(this) { LivePreGameState() }

        return state
    }
}