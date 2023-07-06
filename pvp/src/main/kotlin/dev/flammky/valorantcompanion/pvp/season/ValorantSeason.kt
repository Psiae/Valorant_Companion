package dev.flammky.valorantcompanion.pvp.season

import dev.flammky.valorantcompanion.pvp.date.ISO8601
import kotlinx.collections.immutable.persistentListOf

data class ValorantEpisode(
    val id: String,
    val num: Int,
    val codeName: String,
    val start: ISO8601,
    val end: ISO8601
)

data class ValorantAct(
    val id: String,
    val num: Int,
    val start: ISO8601,
    val end: ISO8601
)

open class ValorantSeason internal constructor(
    val episode: ValorantEpisode,
    val acts: List<ValorantAct>
)

data class ValorantActiveSeason(
    val episode: ValorantEpisode,
    val act: ValorantAct
)


// TODO: add episode 7
object ValorantSeasons {

    val CLOSED_BETA = ValorantSeason(
        ValorantEpisode(
            id = "0df5adb9-4dcb-6899-1306-3e9860661dd3",
            num = 0,
            codeName = "CLOSED BETA",
            start = ISO8601.fromISOString("2020-04-07T13:15:00Z"),
            end = ISO8601.fromISOString("2020-05-30T04:14:00Z")
        ),
        emptyList()
    )

    val EPISODE_1 = ValorantSeason(
        ValorantEpisode(
            id = "fcf2c8f4-4324-e50b-2e23-718e4a3ab046",
            num = 1,
            codeName = "IGNITION",
            start = ISO8601.fromISOString("2020-06-02T04:15:00Z"),
            end = ISO8601.fromISOString("2021-01-12T21:15:00Z")
        ),
        persistentListOf(
            ValorantAct(
                id = "3f61c772-4560-cd3f-5d3f-a7ab5abda6b3",
                num = 1,
                start = ISO8601.fromISOString("2020-06-02T04:15:00Z"),
                end = ISO8601.fromISOString("2020-08-04T21:15:00Z")
            ),
            ValorantAct(
                id = "0530b9c4-4980-f2ee-df5d-09864cd00542",
                num = 2,
                start = ISO8601.fromISOString("2020-08-04T21:15:00Z"),
                end = ISO8601.fromISOString("2020-10-13T21:15:00Z")
            ),
            ValorantAct(
                id = "46ea6166-4573-1128-9cea-60a15640059b",
                num = 3,
                start = ISO8601.fromISOString("2020-10-13T21:15:00Z"),
                end = ISO8601.fromISOString("2021-01-12T21:15:00Z")
            )
        )
    )

    val EPISODE_2 = ValorantSeason(
        ValorantEpisode(
            id = "71c81c67-4fae-ceb1-844c-aab2bb8710fa",
            num = 2,
            codeName = "FORMATION",
            start = ISO8601.fromISOString("2021-01-12T21:15:00Z"),
            end = ISO8601.fromISOString("2021-06-22T21:15:00Z")
        ),
        persistentListOf(
            ValorantAct(
                id = "97b6e739-44cc-ffa7-49ad-398ba502ceb0",
                num = 1,
                start = ISO8601.fromISOString("2021-01-12T21:15:00Z"),
                end = ISO8601.fromISOString("2021-03-02T21:15:00Z")
            ),
            ValorantAct(
                id = "ab57ef51-4e59-da91-cc8d-51a5a2b9b8ff",
                num = 2,
                start = ISO8601.fromISOString("2021-03-02T21:15:00Z"),
                end = ISO8601.fromISOString("2021-04-27T21:15:00Z")
            ),
            ValorantAct(
                id = "52e9749a-429b-7060-99fe-4595426a0cf7",
                num = 3,
                start = ISO8601.fromISOString("2021-04-27T21:15:00Z"),
                end = ISO8601.fromISOString("2021-06-22T21:15:00Z")
            )
        )
    )

    val EPISODE_3 = ValorantSeason(
        ValorantEpisode(
            id = "97b39124-46ce-8b55-8fd1-7cbf7ffe173f",
            num = 3,
            codeName = "REFLECTION",
            start = ISO8601.fromISOString("2021-06-22T21:15:00Z"),
            end = ISO8601.fromISOString("2022-01-11T21:15:00Z")
        ),
        persistentListOf(
            ValorantAct(
                id = "2a27e5d2-4d30-c9e2-b15a-93b8909a442c",
                num = 1,
                start = ISO8601.fromISOString("2021-06-22T21:15:00Z"),
                end = ISO8601.fromISOString("2021-09-08T21:15:00Z")
            ),
            ValorantAct(
                id = "4cb622e1-4244-6da3-7276-8daaf1c01be2",
                num = 2,
                start = ISO8601.fromISOString("2021-09-08T21:15:00Z"),
                end = ISO8601.fromISOString("2021-11-02T21:15:00Z")
            ),
            ValorantAct(
                id = "a16955a5-4ad0-f761-5e9e-389df1c892fb",
                num = 3,
                start = ISO8601.fromISOString("2021-11-02T21:15:00Z"),
                end = ISO8601.fromISOString("2022-01-11T21:15:00Z")
            )
        )
    )

    val EPISODE_4 = ValorantSeason(
        ValorantEpisode(
            id = "808202d6-4f2b-a8ff-1feb-b3a0590ad79f",
            4,
            "DISRUPTION",
            ISO8601.fromISOString("2022-01-11T21:15:00Z"),
            ISO8601.fromISOString("2022-06-21T21:15:00Z")
        ),
        persistentListOf(
            ValorantAct(
                "573f53ac-41a5-3a7d-d9ce-d6a6298e5704",
                1,
                ISO8601.fromISOString("2022-01-11T21:15:00Z"),
                ISO8601.fromISOString("2022-03-01T21:15:00Z")
            ),
            ValorantAct(
                "d929bc38-4ab6-7da4-94f0-ee84f8ac141e",
                2,
                ISO8601.fromISOString("2022-03-01T21:15:00Z"),
                ISO8601.fromISOString("2022-04-26T21:15:00Z")
            ),
            ValorantAct(
                "3e47230a-463c-a301-eb7d-67bb60357d4f",
                3,
                ISO8601.fromISOString("2022-04-26T21:15:00Z"),
                ISO8601.fromISOString("2022-06-21T21:15:00Z")
            )
        )
    )

    val EPISODE_5 = ValorantSeason(
        ValorantEpisode(
            "79f9d00f-433a-85d6-dfc3-60aef115e699",
            5,
            "DIMENSION",
            ISO8601.fromISOString("2022-06-21T21:15:00Z"),
            ISO8601.fromISOString("2023-01-10T21:15:00Z")
        ),
        persistentListOf(
            ValorantAct(
                "67e373c7-48f7-b422-641b-079ace30b427",
                1,
                ISO8601.fromISOString("2022-06-21T21:15:00Z"),
                ISO8601.fromISOString("2022-08-23T21:15:00Z")
            ),
            ValorantAct(
                "7a85de9a-4032-61a9-61d8-f4aa2b4a84b6",
                2,
                ISO8601.fromISOString("2022-08-23T21:15:00Z"),
                ISO8601.fromISOString("2022-10-18T21:15:00Z")
            ),
            ValorantAct(
                "aca29595-40e4-01f5-3f35-b1b3d304c96e",
                3,
                ISO8601.fromISOString("2022-10-18T21:15:00Z"),
                ISO8601.fromISOString("2023-01-10T21:15:00Z")
            )
        )
    )

    val EPISODE_6 = ValorantSeason(
        ValorantEpisode(
            "3ec8084a-4e45-4d22-d801-f8a63e5a208b",
            6,
            "REVELATION",
            ISO8601.fromISOString("2023-01-10T21:15:00Z"),
            ISO8601.fromISOString("2023-06-27T21:15:00Z")
        ),
        persistentListOf(
            ValorantAct(
                "9c91a445-4f78-1baa-a3ea-8f8aadf4914d",
                1,
                ISO8601.fromISOString("2023-01-10T21:15:00Z"),
                ISO8601.fromISOString("2023-03-07T21:15:00Z")
            ),
            ValorantAct(
                "34093c29-4306-43de-452f-3f944bde22be",
                2,
                ISO8601.fromISOString("2023-03-07T21:15:00Z"),
                ISO8601.fromISOString("2023-04-25T21:15:00Z")
            ),
            ValorantAct(
                "2de5423b-4aad-02ad-8d9b-c0a931958861",
                3,
                ISO8601.fromISOString("2023-04-25T21:15:00Z"),
                ISO8601.fromISOString("2023-06-27T21:15:00Z")
            )
        )
    )

    private val ALL by lazy {
        persistentListOf<ValorantSeason>().builder()
            .apply {
                add(CLOSED_BETA)
                add(EPISODE_1)
                add(EPISODE_2)
                add(EPISODE_4)
                add(EPISODE_5)
                add(EPISODE_6)
            }
            .build()
    }

    val ACTIVE_STAGED by lazy {
        ValorantActiveSeason(
            episode = EPISODE_6.episode,
            act = EPISODE_6.acts[2]
        )
    }

    fun asList() = ALL

    fun ofId(id: String): ValorantActiveSeason? {
        ALL.forEach { season ->
            season.acts.find { it.id == id }?.let { return ValorantActiveSeason(season.episode, it) }
        }
        return null
    }
}
