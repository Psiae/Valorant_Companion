package dev.flammky.valorantcompanion.base.compose

import androidx.annotation.CallSuper
import androidx.compose.runtime.RememberObserver
import dev.flammky.valorantcompanion.base.inMainLooper

interface BaseRememberObserver : RememberObserver {

    @CallSuper
    override fun onAbandoned() {
        check(inMainLooper())
    }

    @CallSuper
    override fun onForgotten() {
        check(inMainLooper())
    }

    @CallSuper
    override fun onRemembered() {
        check(inMainLooper())
    }
}