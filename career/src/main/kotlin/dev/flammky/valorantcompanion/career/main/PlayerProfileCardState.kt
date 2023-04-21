package dev.flammky.valorantcompanion.career.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class PlayerProfileCardState() {

    var profilePicture by mutableStateOf<Any?>(null)
    var riotId by mutableStateOf<String?>(null)
    var tagLine by mutableStateOf<String?>(null)
}