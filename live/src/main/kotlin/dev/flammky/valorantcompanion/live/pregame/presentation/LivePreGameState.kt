package dev.flammky.valorantcompanion.live.pregame.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class LivePreGameState() {
    var mapName by mutableStateOf<String>("")
    var gameModeName by mutableStateOf<String>("")
    var gamePodName by mutableStateOf<String>("")
    var countDown by mutableStateOf<Int>(-1)
    var ally by mutableStateOf<PreGameTeam?>(null)
    var enemy by mutableStateOf<PreGameTeam?>(null)
    var isProvisioned by mutableStateOf(false)
}