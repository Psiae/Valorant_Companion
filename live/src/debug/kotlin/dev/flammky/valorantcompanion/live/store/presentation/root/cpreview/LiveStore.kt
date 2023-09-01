package dev.flammky.valorantcompanion.live.store.presentation.root.cpreview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Surface
import dev.flammky.valorantcompanion.live.store.presentation.root.LiveStore
import dev.flammky.valorantcompanion.live.store.presentation.root.LiveStoreState

@Preview
@Composable
fun LiveStorePreview() {
    DefaultMaterial3Theme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .localMaterial3Surface()
        ) {
            LiveStore(
                modifier = Modifier,
                isVisibleToUser = true,
                openScreen = {}
            )
        }
    }
}