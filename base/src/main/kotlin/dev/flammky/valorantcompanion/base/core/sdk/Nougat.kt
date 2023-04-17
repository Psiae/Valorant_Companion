package dev.flammky.valorantcompanion.base.core.sdk

import android.os.Build
import dev.flammky.valorantcompanion.base.core.sdk.AndroidAPI
import dev.flammky.valorantcompanion.base.core.sdk.BuildCode

object Nougat : AndroidAPI() {
    override val code: BuildCode = BuildCode(Build.VERSION_CODES.N)
}

object Nougat_MR1 : AndroidAPI() {
    override val code: BuildCode = BuildCode(Build.VERSION_CODES.N_MR1)
}