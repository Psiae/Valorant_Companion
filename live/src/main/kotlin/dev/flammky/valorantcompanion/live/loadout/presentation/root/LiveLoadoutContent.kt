package dev.flammky.valorantcompanion.live.loadout.presentation.root

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.theme.material3.interactiveUiElementAlphaEnforcement
import dev.flammky.valorantcompanion.base.theme.material3.interactiveUiElementSizeEnforcement
import dev.flammky.valorantcompanion.base.theme.material3.interactiveUiElementTextAlphaEnforcement

@Composable
internal fun LiveLoadoutContent(
    weaponEnabled: Boolean,
    openWeaponScreen: () -> Unit,
    sprayEnabled: Boolean,
    openSprayScreen: () -> Unit,
) {
    BoxWithConstraints {
        val maxWidth = maxWidth
        Column {
            Row {
                OpenWeaponScreenCard(
                    modifier = Modifier
                        .size(maxWidth / 2 - 8.dp)
                        .padding(start = 16.dp, top = 16.dp)
                        .interactiveUiElementSizeEnforcement()
                        .interactiveUiElementTextAlphaEnforcement(
                            isContent = false,
                            enabled = weaponEnabled
                        )
                        .clickable(enabled = weaponEnabled, onClick = openWeaponScreen),
                    iconModifier = Modifier
                        .interactiveUiElementAlphaEnforcement(
                            isContent = true,
                            enabled = weaponEnabled
                        ),
                    textModifier = Modifier
                        .interactiveUiElementTextAlphaEnforcement(
                            isContent = true,
                            enabled = weaponEnabled
                        )
                )

                Spacer(modifier = Modifier.width(16.dp))

                OpenSprayScreenCard(
                    modifier = Modifier
                        .size(maxWidth / 2 - 8.dp)
                        .padding(end = 16.dp, top = 16.dp)
                        .interactiveUiElementSizeEnforcement()
                        .interactiveUiElementTextAlphaEnforcement(
                            isContent = false,
                            enabled = sprayEnabled
                        )
                        .clickable(enabled = sprayEnabled, onClick = openSprayScreen),
                    iconModifier = Modifier
                        .interactiveUiElementAlphaEnforcement(
                            isContent = true,
                            enabled = sprayEnabled
                        ),
                    textModifier = Modifier
                        .interactiveUiElementTextAlphaEnforcement(
                            isContent = true,
                            enabled = sprayEnabled
                        )
                )
            }
        }
    }
}