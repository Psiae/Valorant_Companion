package dev.flammky.valorantcompanion.pvp.store.weapon.skin

sealed class WeaponSkinTier(
    val uuid: String,
    val displayName: String,
    val codeName: String,
    val rank: Int,
    val highlightColor: Long
) {

    object SELECT : WeaponSkinTier(
        uuid = "12683d76-48d7-84a3-4e09-6985794f0445",
        displayName = "Select Edition",
        codeName = "Select",
        rank = 0,
        highlightColor = 0x335A9FE2
    )

    object DELUXE : WeaponSkinTier(
        uuid = "0cebb8be-46d7-c12a-d306-e9907bfc5a25",
        displayName = "Deluxe Edition",
        codeName = "Deluxe",
        rank = 1,
        highlightColor = 0x33009587
    )

    object PREMIUM : WeaponSkinTier(
        uuid = "60bca009-4182-7998-dee7-b8a2558dc369",
        displayName = "Premium Edition",
        codeName = "Premium",
        rank = 2,
        highlightColor = 0x33D1548D
    )

    object EXCLUSIVE : WeaponSkinTier(
        uuid = "e046854e-406c-37f4-6607-19a9ba8426fc",
        displayName = "Exclusive Edition",
        codeName = "Exclusive",
        rank = 3,
        highlightColor = 0x33F5955B
    )

    object ULTRA : WeaponSkinTier(
        uuid = "411e4a55-4e59-7757-41f0-86a53f101bb5",
        displayName = "Ultra Edition",
        codeName = "Ultra",
        rank = 4,
        highlightColor = 0x33FAD663
    )
}
