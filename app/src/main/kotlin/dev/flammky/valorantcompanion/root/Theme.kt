package dev.flammky.valorantcompanion.root

import androidx.compose.runtime.Composable
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme

@Composable
fun ValorantCompanionTheme(
    content: @Composable () -> Unit
) = DefaultMaterial3Theme(content = content)