package dev.flammky.valorantcompanion.live.ingame.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.assets.R as R_ASSET

@Composable
@Preview
internal fun FakeLiveInGameTopBarPreview() = DefaultMaterial3Theme(dark = true) {
    FakeLiveInGameTopBar()
}


@Composable
internal fun FakeLiveInGameTopBar() = LiveInGameTopBar(
    modifier = Modifier,
    mapName = "Ascent",
    gameModeName = "Spike Rush",
    gamePodName = "Singapore-1",
    pingMs = 31,
    mapImage = LocalImage.Resource(R_ASSET.raw.ascent_listviewicon)
)