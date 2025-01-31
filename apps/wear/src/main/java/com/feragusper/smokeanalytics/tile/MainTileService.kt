package com.feragusper.smokeanalytics.tile

import androidx.lifecycle.lifecycleScope
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.ChipDefaults
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileUpdateRequester
import com.feragusper.smokeanalytics.libraries.architecture.presentation.BuildConfig
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val RESOURCES_VERSION = "1"

@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    private val viewModel = TileViewModel.apply {
        initialize(this@MainTileService)
    }

    private val tileUpdateRequester: TileUpdateRequester by lazy {
        getUpdater(this)
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG && Timber.forest().isEmpty()) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("onCreate")

        lifecycleScope.launch {
            viewModel.states()
                .distinctUntilChanged { old, new -> old.smokesPerDay == new.smokesPerDay }
                .collect {
                    Timber.d("onCreate: collect: $it")
                    tileUpdateRequester.requestUpdate(MainTileService::class.java)
                }
        }
    }

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val state = viewModel.states().value

        if (requestParams.currentState.lastClickableId == "add_smoke_action") {
            Timber.d("Add Smoke clicked! Updating ViewModel...")
            viewModel.intents().trySend(TileIntent.AddSmoke)
        }

        return createTile(state)
    }

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        Timber.d("resourcesRequest")

        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping(
                "smoke_icon",
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(com.feragusper.smokeanalytics.libraries.design.R.drawable.ic_cigarette)
                            .build()
                    ).build()
            )
            .build()
    }

    private fun createTile(state: TileViewState): TileBuilders.Tile {
        val layout = createTileLayout(state)
        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(layout)
                            .build()
                    ).build()
            ).build()
    }

    private fun createTileLayout(state: TileViewState): LayoutElementBuilders.Layout {
        val deviceParameters = DeviceParametersBuilders.DeviceParameters.Builder()
            .setScreenWidthDp(resources.configuration.screenWidthDp)
            .setScreenHeightDp(resources.configuration.screenHeightDp)
            .build()

        val lastSmokeText = formatLastSmokeTime(state.lastSmokeTimestamp)

        val statsText = Text.Builder(this, "Today: ${state.smokesPerDay ?: 0}")
            .setTypography(Typography.TYPOGRAPHY_TITLE3)
            .setColor(androidx.wear.protolayout.ColorBuilders.argb(0xFF00897B.toInt()))
            .setMaxLines(1)
            .build()

        val lastSmoke = Text.Builder(this, lastSmokeText)
            .setTypography(Typography.TYPOGRAPHY_BODY2)
            .setColor(androidx.wear.protolayout.ColorBuilders.argb(0xFF00897B.toInt()))
            .setMaxLines(1)
            .build()

        val addSmokeChip = CompactChip.Builder(
            this,
            androidx.wear.protolayout.ModifiersBuilders.Clickable.Builder()
                .setId("add_smoke_action")
                .setOnClick(ActionBuilders.LoadAction.Builder().build())
                .build(),
            deviceParameters
        )
            .setIconContent("smoke_icon")
            .setChipColors(ChipDefaults.PRIMARY_COLORS)
            .build()

        return LayoutElementBuilders.Layout.Builder()
            .setRoot(
                PrimaryLayout.Builder(deviceParameters)
                    .setPrimaryLabelTextContent(statsText)
                    .setContent(lastSmoke)
                    .setPrimaryChipContent(addSmokeChip)
                    .setResponsiveContentInsetEnabled(true)
                    .build()
            )
            .build()
    }

    private fun formatLastSmokeTime(timestamp: Long?): String {
        if (timestamp == null || timestamp == 0L) return "Last Smoke: N/A"

        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Last Smoke: Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "Last Smoke: ${TimeUnit.MILLISECONDS.toMinutes(diff)} min ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "Last Smoke: ${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
            else -> "Last Smoke: ${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
        }
    }

}
