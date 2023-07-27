package dev.flammky.valorantcompanion.live.main

import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead

interface LiveMainScreenScope {

    val hasFocus: Boolean
        @SnapshotRead get

    fun dismiss()
}