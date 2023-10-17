package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.dpPaddingIncrementsOf
import dev.flammky.valorantcompanion.pvp.store.FeaturedBundleStore
import kotlinx.collections.immutable.ImmutableList


@Composable
fun FeaturedBundleDisplayPager(
    modifier: Modifier,
    bundleCount: Int,
    getBundle: @SnapshotRead (Int) -> FeaturedBundleStore.Bundle,
    pageModifier: @SnapshotRead (Int) -> Modifier,
    pageShape: @SnapshotRead (Int) -> Shape,
    isVisibleToUser: Boolean,
    showIndicator: Boolean,
) {
    FeaturedBundleDisplayPager(
        modifier = modifier,
        presenter = rememberFeaturedBundleDisplayPresenter(di = LocalDependencyInjector.current),
        bundleCount = bundleCount,
        getBundle = getBundle,
        pageModifier = pageModifier,
        pageShape = pageShape,
        isVisibleToUser = isVisibleToUser,
        showIndicator = showIndicator
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeaturedBundleDisplayPager(
    modifier: Modifier,
    presenter: FeaturedBundleDisplayPresenter,
    bundleCount: Int,
    getBundle: @SnapshotRead (Int) -> FeaturedBundleStore.Bundle,
    pageModifier: @SnapshotRead (Int) -> Modifier,
    pageShape: @SnapshotRead (Int) -> Shape,
    isVisibleToUser: Boolean,
    showIndicator: Boolean
) {
    Column(modifier.fillMaxSize()) {

        val pagerState = rememberPagerState()
        HorizontalPager(
            state = pagerState,
            pageCount = bundleCount.coerceAtMost(4),
            beyondBoundsPageCount = bundleCount.coerceAtMost(4),
            // TODO: figure out why it has padding at the last page
            pageSpacing = Material3Theme.dpPaddingIncrementsOf(3)
        ) { i ->
            val bundle = remember(i, getBundle) {
                derivedStateOf(neverEqualPolicy()) { getBundle(i) }
            }
            val bundleKey = remember(i, getBundle) {
                derivedStateOf(neverEqualPolicy()) { bundle.value ; Any() }
            }
            FeaturedBundleDisplay(
                modifier = pageModifier.invoke(i),
                state = presenter.present(
                    offerKey = bundleKey.value,
                    offer = bundle.value,
                    isVisibleToUser = run {
                        isVisibleToUser &&
                                i == pagerState.currentPage ||
                                i == pagerState.currentPage - 1 && pagerState.currentPageOffsetFraction < 0.0 ||
                                i == pagerState.currentPage + 1 && pagerState.currentPageOffsetFraction > 0.0
                    }
                ),
                shape = pageShape.invoke(i)
            )
        }

        if (showIndicator) {
            Spacer(modifier = Modifier.height(Material3Theme.dpPaddingIncrementsOf(2)))

            // TODO: Indicator
        }
    }
}