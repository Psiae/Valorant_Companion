package dev.flammky.valorantcompanion.base.core.sdk

import android.os.Build
import dev.flammky.valorantcompanion.base.core.sdk.AndroidAPI
import dev.flammky.valorantcompanion.base.core.sdk.BuildCode

object Pie : AndroidAPI() {
    override val code: BuildCode = BuildCode(Build.VERSION_CODES.P)
}