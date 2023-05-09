package dev.flammky.valorantcompanion.assets.map

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet

class LoadMapImageRequest private constructor(
    val uuid: String,
    val acceptableTypes: ImmutableSet<ValorantMapImageType>,
) {


    class Builder()  {
        private var _displayName: String? = null
        private var _uuid: String? = null
        private var _assetName: String? = null
        private var _assetUrl: String? = null
        private var _acceptableTypes: PersistentSet<ValorantMapImageType>? = null

        fun build(): LoadMapImageRequest = LoadMapImageRequest(
            uuid = _uuid
                ?: _assetName?.let { resolveAssetName(it) }
                ?: _assetUrl?.let { resolveAssetUrl(it) }
                ?: _displayName?.let { resolveDisplayName(it) }
                ?: "",
            acceptableTypes = _acceptableTypes
                ?: persistentSetOf()
        )

        fun displayName(name: String) {
            _displayName = name
        }

        fun uuid(uuid: String) {
            _uuid = uuid
        }

        fun assetName(assetName: String) {
            _assetName = assetName
        }

        fun assetUrl(url: String) {
            _assetUrl = url
        }

        fun acceptableTypes(vararg types: ValorantMapImageType) {
            _acceptableTypes = types.asIterable().toPersistentSet()
        }

        private fun resolveAssetName(name: String): String {
            ValorantMapAsset.iter().forEach {
                if (it.code_name.lowercase() == name.lowercase()) return it.uuid
            }
            return ""
        }

        private fun resolveDisplayName(name: String): String {
            ValorantMapAsset.iter().forEach {
                if (it.display_name.lowercase() == name.lowercase()) return it.uuid
            }
            return ""
        }

        private fun resolveAssetUrl(url: String): String {
            ValorantMapAsset.iter().forEach {
                if (it.asset_url.lowercase() == url.lowercase()) return it.uuid
            }
            return ""
        }
    }
}

fun LoadMapImageRequest(
    build: LoadMapImageRequest.Builder.() -> Unit
) = LoadMapImageRequest.Builder().apply(build).build()