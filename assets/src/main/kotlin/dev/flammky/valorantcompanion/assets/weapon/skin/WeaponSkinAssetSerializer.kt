package dev.flammky.valorantcompanion.assets.weapon.skin

import java.nio.charset.Charset

interface WeaponSkinAssetSerializer {

    fun deserializeIdentity(
        raw: ByteArray,
        charset: Charset
    ): Result<WeaponSkinIdentity>

    fun deserializeIdentity(
        raw: String
    ): Result<WeaponSkinIdentity>

    fun deserializeSkinsAssets(
        raw: String
    ): Result<WeaponSkinsAssets>
}