package dev.flammky.valorantcompanion.assets.di

import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.filesystem.AndroidFileSystem
import dev.flammky.valorantcompanion.assets.internal.ValorantAssetRepository
import dev.flammky.valorantcompanion.assets.internal.ValorantAssetsServiceImpl
import dev.flammky.valorantcompanion.assets.spray.KtxValorantSprayAssetSerializer
import dev.flammky.valorantcompanion.assets.weapon.skin.KtxWeaponSkinAssetSerializer
import org.koin.dsl.module

val KoinAssetsModule = module {
    single<ValorantAssetRepository> {
        ValorantAssetRepository(
            AndroidFileSystem(get()),
            KtxValorantSprayAssetSerializer(),
            KtxWeaponSkinAssetSerializer()
        )
    }
    single<ValorantAssetsService> { ValorantAssetsServiceImpl(get()) }
}