package dev.flammky.valorantcompanion.base.compose

import androidx.compose.runtime.compositionLocalOf

interface DismissHandler {

    fun dismiss()
}

val LocalDismissHandler = compositionLocalOf<DismissHandler>(
    defaultFactory = { error("LocalDismissHandler not provided") }
)