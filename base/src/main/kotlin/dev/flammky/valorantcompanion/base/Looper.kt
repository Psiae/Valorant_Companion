package dev.flammky.valorantcompanion.base

import android.os.Looper

fun inMainLooper(): Boolean {
    return Looper.myLooper()
        ?.let { looper ->
            looper == Looper.getMainLooper()
        } == true
}

fun checkInMainLooper() = check(
    value = inMainLooper(),
    lazyMessage = {
        "Invalid Thread Access, expected Main, " +
                "current=${Thread.currentThread().name};looper=${Looper.myLooper()}"
    }
)