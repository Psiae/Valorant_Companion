package dev.flammky.valorantcompanion.base

import android.os.Looper

fun inMainLooper(): Boolean {
    return Looper.myLooper()
        ?.let { looper ->
            looper == Looper.getMainLooper()
        } == true
}