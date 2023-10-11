package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.cpreview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.debug.DebugValorantAssetService
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.compose.compose
import dev.flammky.valorantcompanion.base.compose.rememberUpdatedStateWithKey
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.koin.compose.KoinDependencyInjector
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.WeaponSkinOfferCard
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.rememberWeaponSkinOfferCardPresenter
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreService
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import dev.flammky.valorantcompanion.pvp.store.currency.ValorantPoint
import dev.flammky.valorantcompanion.pvp.store.debug.StubValorantStoreService
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
@Preview
private fun WeaponSkinOfferPanelPreview() {

    val provisionedState = remember {
        mutableStateOf<Boolean>(false)
    }

    LaunchedEffect(Unit) {
        GlobalContext.stopKoin()
        GlobalContext.startKoin {
            modules(
                module {
                    single<ValorantAssetsService> {
                        DebugValorantAssetService()
                    }
                    single<ValorantStoreService> {
                        // TODO
                        StubValorantStoreService(
                            { null },
                            { null }
                        )
                    }
                }
            )
        }
        provisionedState.value = true
    }

    DefaultMaterial3Theme(dark = isSystemInDarkTheme()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .localMaterial3Surface()
                .verticalScroll(rememberScrollState())
                .padding(
                    vertical = Material3Theme.dpPaddingIncrementsOf(2),
                    horizontal = Material3Theme.dpPaddingIncrementsOf(4)
                )
        ) {

            if (!provisionedState.value) return@Box

            CompositionLocalProvider(
                LocalDependencyInjector provides remember { KoinDependencyInjector(GlobalContext) }
            ) {
                WeaponSkinOfferPanel(
                    modifier = Modifier,
                    durationLeft = 15.hours + 10.minutes + 20.seconds,
                    itemKey = Any(),
                    items = persistentListOf(
                        WeaponSkinOfferPanelItem(
                            uuid = "dbg_wpn_skn_exc_neofrontier_melee",
                            cost = StoreCost(ValorantPoint, 4_350),
                        ),
                        WeaponSkinOfferPanelItem(
                            uuid = "dbg_wpn_skn_ult_spectrum_phantom",
                            cost = StoreCost(ValorantPoint, 2_675),
                        ),
                        WeaponSkinOfferPanelItem(
                            uuid = "dbg_wpn_skn_prmm_oni_shorty",
                            cost = StoreCost(ValorantPoint, 1_775),
                        ),
                        WeaponSkinOfferPanelItem(
                            uuid = "dbg_wpn_skn_dlx_sakura_stinger",
                            cost = StoreCost(ValorantPoint, 1_275),
                        ),
                        WeaponSkinOfferPanelItem(
                            uuid = "dbg_wpn_skn_slct_endeavour_bulldog",
                            cost = StoreCost(ValorantPoint, 875),
                        )
                    ),
                    canOpenDetail = { true },
                    openDetail = {  }
                )
            }
        }
    }
}

@Immutable
class WeaponSkinOfferPanelItem(
    val uuid: String,
    val cost: StoreCost,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WeaponSkinOfferPanelItem) return false

        other as WeaponSkinOfferPanelItem

        return uuid == other.uuid &&
                cost == other.cost
    }

    override fun hashCode(): Int {
        var result = 0
        result += uuid.hashCode()
        result *= 31 ; result += cost.hashCode()
        return result
    }
}

@Composable
fun WeaponSkinOfferPanel(
    modifier: Modifier,
    durationLeft: Duration,
    itemKey: Any,
    items: ImmutableList<WeaponSkinOfferPanelItem>,
    canOpenDetail: (WeaponSkinOfferPanelItem) -> Boolean,
    openDetail: (WeaponSkinOfferPanelItem) -> Unit
) {

    val upOpenDetail = rememberUpdatedState(newValue = openDetail)

    Column(modifier = modifier) {

        DailyOfferHeader(
            modifier = Modifier,
            durationLeft = durationLeft
        )

        Spacer(modifier = Modifier.height(Material3Theme.dpPaddingIncrementsOf(3)))

        compose(rememberUpdatedStateWithKey(key = itemKey, value = items)) { itemsState ->

            itemsState.value.forEachIndexed { i, item ->

                WeaponSkinOfferCard(
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth(),
                    state = rememberWeaponSkinOfferCardPresenter(
                        LocalDependencyInjector.current
                    ).present(
                        id = item.uuid,
                        cost = item.cost
                    ),
                    canOpenDetail = canOpenDetail(item),
                    openDetail = { upOpenDetail.value.invoke(item) },
                    shape = RoundedCornerShape(12.dp)
                )

                if (i != itemsState.value.lastIndex) {
                    Spacer(modifier = Modifier.height(Material3Theme.dpPaddingIncrementsOf(3)))
                }
            }
        }
    }
}

@Composable
private fun DailyOfferHeader(
    modifier: Modifier,
    durationLeft: Duration
) {
    Row(modifier) {

        BasicText(
            modifier = Modifier.weight(1f),
            text = "Daily Offer",
            style = MaterialTheme3.typography.titleMedium.copy(
                color = Material3Theme.surfaceContentColorAsState().value
            )
        )

        Row(modifier = Modifier.align(Alignment.CenterVertically)) {

            BasicText(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = remember(durationLeft) {
                    durationLeft.toComponents { _, hours, minutes, seconds, _ ->
                        "${hours}h ${minutes}:${seconds}"
                    }
                },
                style = MaterialTheme3.typography.labelLarge.copy(
                    color = Material3Theme.surfaceContentColorAsState().value
                )
            )

            Spacer(modifier = Modifier.width(Material3Theme.dpPaddingIncrementsOf(2)))

            Icon(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = R_ASSET_DRAWABLE.time_machine_ios_16_glyph_100px),
                contentDescription = null,
                tint = Material3Theme.surfaceContentColorAsState().value
            )
        }
    }
}