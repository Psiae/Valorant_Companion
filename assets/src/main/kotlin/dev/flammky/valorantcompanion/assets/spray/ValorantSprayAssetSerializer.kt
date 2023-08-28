package dev.flammky.valorantcompanion.assets.spray

import java.nio.charset.Charset

interface ValorantSprayAssetSerializer {

    fun deserializeIdentity(
        uuid: String,
        raw: ByteArray,
        charset: Charset
    ): Result<ValorantSprayAssetIdentity>

    fun deserializeIdentity(
        uuid: String,
        raw: String
    ): Result<ValorantSprayAssetIdentity>
}