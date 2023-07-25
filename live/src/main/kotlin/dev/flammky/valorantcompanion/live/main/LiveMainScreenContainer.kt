package dev.flammky.valorantcompanion.live.main

import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead

interface LiveMainScreenContainer {

    val isVisible: Boolean
        @SnapshotRead get

    fun dismiss()
}