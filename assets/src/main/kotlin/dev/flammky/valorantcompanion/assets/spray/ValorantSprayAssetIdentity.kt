package dev.flammky.valorantcompanion.assets.spray

import kotlinx.collections.immutable.ImmutableList

data class ValorantSprayAssetIdentity(
    val uuid: String,
    val displayName: String,
    val category: Category,
    val levels: ImmutableList<Level>
) {

    sealed class Category(
        val codeName: String
    ) {
        object Contextual : Category("EAresSprayCategory::Contextual")
        // we define these
        object None : Category("This::None")
        object Other : Category("This::Other")

        companion object {
            fun parse(
                string: String?
            ): Category {
                return when(string ?: return None) {
                    Contextual.codeName -> Contextual
                    "null" -> None
                    else -> Other
                }
            }
        }
    }

    data class Level(
        val uuid: String,
        val sprayLevel: Int,
        val displayName: String
    )
}

val ValorantSprayAssetIdentity.isContextual
    get() = category == ValorantSprayAssetIdentity.Category.Contextual