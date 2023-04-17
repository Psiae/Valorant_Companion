package dev.flammky.valorantcompanion.base.core.sdk

import android.os.Build

object AndroidR : AndroidAPI() {
    override val code: BuildCode = BuildCode(Build.VERSION_CODES.R)
}