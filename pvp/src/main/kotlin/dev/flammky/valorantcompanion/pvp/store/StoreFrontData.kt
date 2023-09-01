package dev.flammky.valorantcompanion.pvp.store

data class StoreFrontData(
    val featuredBundle: FeaturedBundle,
    val skinsPanel: SkinsPanelData,
    val upgradeCurrencyStore: UpgradeCurrencyStoreData,
    val accessoryStore: AccessoryStoreData,
    val bonusStore: BonusStoreData
) {

}