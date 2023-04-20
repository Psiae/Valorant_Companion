package dev.flammky.valorantcompanion.main

import dev.flammky.valorantcompanion.R

object MainNavigationDestinations {

    val Career = MainNavigationDestination(
        id = "career",
        label = "Career",
        resId = R.drawable.timeline_clock_512px
    )

    val Live = MainNavigationDestination(
        id = "live",
        label = "Live",
        resId = R.drawable.ios_glyph_play_100px
    )

    val asList by lazy {
        listOf(Career, Live)
    }
}