package dev.flammky.valorantcompanion.base.compose

import androidx.annotation.CallSuper
import dev.flammky.valorantcompanion.base.checkInMainLooper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

interface RememberObserver : androidx.compose.runtime.RememberObserver {

    @CallSuper
    override fun onAbandoned() {
        checkInMainLooper()
    }

    @CallSuper
    override fun onForgotten() {
        checkInMainLooper()
    }

    @CallSuper
    override fun onRemembered() {
        checkInMainLooper()
    }
}

// TODO:
class RememberObserverBehavior : RememberObserver {

    private var abandoned = false
    private var forgotten = false
    private var remembered = false

    override fun onAbandoned() {
        super.onAbandoned()
        check(!remembered)
        check(!forgotten)
        check(!abandoned)
        abandoned = true
    }

    override fun onForgotten() {
        super.onForgotten()
        check(remembered)
        check(!forgotten)
        check(!abandoned)
        forgotten = true
    }

    override fun onRemembered() {
        super.onRemembered()
        check(!remembered)
        check(!forgotten)
        check(!abandoned)
        remembered = true
    }
}