package dev.flammky.valorantcompanion.pvp.map

import kotlinx.collections.immutable.persistentListOf
import kotlin.reflect.KClass

sealed class ValorantMapIdentity(
    val uuid: String,
    val display_name: String,
    val coordinates: String,
    val map_id: String,
    val code_name: String
) {

    object ASCENT : ValorantMapIdentity(
        uuid = "7eaecc1b-4337-bbf6-6ab9-04b8f06b3319",
        display_name = "Ascent",
        coordinates = "45°26'BF'N,12°20'Q'E",
        map_id = "/Game/Maps/Ascent/Ascent",
        code_name = "Ascent"
    )

    object BIND : ValorantMapIdentity(
        uuid = "2c9d57ec-4431-9c5e-2939-8f9ef6dd5cba",
        display_name = "Bind",
        coordinates = "34°2'A'N,6°51'Z'W",
        map_id = "/Game/Maps/Duality/Duality",
        code_name = "Duality"
    )

    object BREEZE : ValorantMapIdentity(
        uuid = "2fb9a4fd-47b8-4e7d-a969-74b4046ebd53",
        display_name = "Breeze",
        coordinates = "26°11'AG\"N 71°10'WY\"W",
        map_id = "/Game/Maps/Foxtrot/Foxtrot",
        code_name = "Foxtrot"
    )

    object FRACTURE : ValorantMapIdentity(
        uuid = "b529448b-4d60-346e-e89e-00a4c527a405",
        display_name = "Fracture",
        coordinates = "35°48'BI\"N 106°08'YQ\"W",
        map_id = "/Game/Maps/Canyon/Canyon",
        code_name = "Canyon"
    )

    object HAVEN : ValorantMapIdentity(
        uuid = "2bee0dc9-4ffe-519b-1cbd-7fbe763a6047",
        display_name = "Haven",
        coordinates = "27°28'A'N,89°38'WZ'E",
        map_id = "/Game/Maps/Triad/Triad",
        code_name = "Triad"
    )

    object ICEBOX : ValorantMapIdentity(
        uuid = "e2ad5c54-4114-a870-9641-8ea21279579a",
        display_name = "Icebox",
        coordinates = "76°44' A\"N 149°30' Z\"E",
        map_id = "/Game/Maps/Port/Port",
        code_name = "Port"
    )

    object LOTUS : ValorantMapIdentity(
        uuid = "2fe4ed3a-450a-948b-6d6b-e89a78e680a9",
        display_name = "Lotus",
        coordinates = "14°07'AD.4\"N8 74°53'XY\"E8",
        map_id = "/Game/Maps/Jam/Jam",
        code_name = "Jam"
    )

    object THE_RANGE : ValorantMapIdentity(
        uuid = "ee613ee9-28b7-4beb-9666-08db13bb2244",
        display_name = "The Range",
        coordinates = "45°26'FF'N,12°20'Q'E",
        map_id = "/Game/Maps/Poveglia/Range",
        code_name = "Range"
    )

    object PEARL : ValorantMapIdentity(
        uuid = "fd267378-4d1d-484f-ff52-77821ed10dc2",
        display_name = "Pearl",
        coordinates = "38°42'ED\"N8 9°08'XS\"W8\n",
        map_id = "/Game/Maps/Pitt/Pitt",
        code_name = "Pitt"
    )

    object SPLIT : ValorantMapIdentity(
        uuid = "d960549e-485c-e861-8d71-aa9d1aed12a2",
        display_name = "Split",
        coordinates = "35°41'CD'N,139°41'WX'E",
        map_id = "/Game/Maps/Bonsai/Bonsai",
        code_name = "Bonsai"
    )

    companion object {
        private val SUBCLASSES by lazy {
            ValorantMapIdentity::class.sealedSubclasses.mapNotNullTo(
                destination = persistentListOf<ValorantMapIdentity>().builder(),
                transform = KClass<out ValorantMapIdentity>::objectInstance
            ).build()
        }


        fun ofID(id: String): ValorantMapIdentity? {
            return SUBCLASSES.find { it.map_id == id }
        }
    }
}
