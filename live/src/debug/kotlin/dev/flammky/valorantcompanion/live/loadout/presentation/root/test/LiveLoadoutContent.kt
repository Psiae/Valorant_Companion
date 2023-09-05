package dev.flammky.valorantcompanion.live.loadout.presentation.root.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Background
import dev.flammky.valorantcompanion.live.loadout.presentation.root.LiveLoadoutContent

@Composable
@Preview
private fun LiveLoadoutContentPreview() {
    DefaultMaterial3Theme(
        dark = true
    ) {
        Box(modifier = Modifier.fillMaxSize().localMaterial3Background()) {
            LiveLoadoutContent(
                weaponEnabled = false,
                openWeaponScreen = {},
                sprayEnabled = true,
                openSprayScreen = {},
            )
        }
    }
}