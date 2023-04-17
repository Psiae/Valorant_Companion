package dev.flammky.valorantcompanion.base.core.sdk

import android.os.Build
import dev.flammky.valorantcompanion.base.core.sdk.AndroidAPI
import dev.flammky.valorantcompanion.base.core.sdk.BuildCode

object SnowCone : AndroidAPI() {
    override val code: BuildCode = BuildCode(Build.VERSION_CODES.S)
}

object SnowConeV2 : AndroidAPI() {
    override val code: BuildCode = BuildCode(Build.VERSION_CODES.S_V2)
}