package com.feragusper.smokeanalytics.tile

import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.feragusper.smokeanalytics.libraries.wear.data.WearSyncManager
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.minutes

private const val RESOURCES_VERSION = "1"

@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    private val viewModel: TileViewModel by lazy {
        TileViewModel(TileProcessHolder(WearSyncManager(this)))
    }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {

        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {

        val state = viewModel.states().first()
        val smokeCount = state.smokesPerDay ?: 0

        val layout = createTileLayout(smokeCount)

        val timeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(layout)
                    .build()
            )
            .build()

        return TileBuilders.Tile.Builder()
            .setFreshnessIntervalMillis(5.minutes.inWholeMilliseconds)
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(timeline)
            .build()
    }

    private fun createTileLayout(smokeCount: Int): LayoutElementBuilders.Layout {
        val deviceParameters = DeviceParametersBuilders.DeviceParameters.Builder()
            .setScreenWidthDp(resources.configuration.screenWidthDp)
            .setScreenHeightDp(resources.configuration.screenHeightDp)
            .build()

        val content = PrimaryLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .setContent(
                Text.Builder(this, "Puchos hoy: $smokeCount")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .build()
            )
            .build()

        return LayoutElementBuilders.Layout.Builder()
            .setRoot(content)
            .build()
    }
}
