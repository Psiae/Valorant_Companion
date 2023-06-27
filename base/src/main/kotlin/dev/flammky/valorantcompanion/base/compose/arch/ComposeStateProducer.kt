package dev.flammky.valorantcompanion.base.compose.arch

import androidx.compose.runtime.Composable
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead
import dev.flammky.valorantcompanion.base.compose.state.SnapshotWrite

abstract class ComposeStateProducer <T> () where T: Any {

    protected abstract var state: T

    @Composable
    @SnapshotRead
    abstract fun produceState(): T

    @SnapshotWrite
    protected abstract fun mutateState(
        actionName: String,
        mutate: (T) -> Unit
    )
}