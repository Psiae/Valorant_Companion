package dev.flammky.valorantcompanion.base.core.sdk

import android.os.Build
import dev.flammky.valorantcompanion.base.core.sdk.AndroidAPI
import dev.flammky.valorantcompanion.base.core.sdk.BuildCode

object Oreo : AndroidAPI() {
    override val code: BuildCode = BuildCode(Build.VERSION_CODES.O)
}

object OreoMR1 : AndroidAPI() {
    override val code: BuildCode = BuildCode(Build.VERSION_CODES.O_MR1)
}