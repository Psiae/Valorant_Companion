package dev.flammky.valorantcompanion.pvp.store

data class StoreFrontData(
    val featuredBundleStore: FeaturedBundleStore,
    val skinsPanel: SkinsPanelStore,
    val upgradeCurrencyStore: UpgradeCurrencyStore,
    val accessoryStore: AccessoryStore,
    val bonusStore: BonusStore
) {

}