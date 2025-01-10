package com.feragusper.smokeanalytics.tile

import android.content.Context
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.TileUpdateRequester
import com.feragusper.smokeanalytics.BuildConfig
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

private const val RESOURCES_VERSION = "1"

@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    private val viewModel = TileViewModel

    private val tileUpdateRequester: TileUpdateRequester by lazy {
        getUpdater(this)
    }

    init {
        if (BuildConfig.DEBUG) {
//            Timber.plant(Timber.DebugTree())
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
        lifecycleScope.launch {
            Timber.d("onCreate: requestUpdate")
            viewModel
                .states()
                .distinctUntilChanged { old, new ->
                    old.smokesPerDay == new.smokesPerDay
                }.collect {
                    Timber.d("onCreate: collect: $it")
                    tileUpdateRequester.requestUpdate(MainTileService::class.java)
                }
        }
    }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {

        Timber.d("resourcesRequest")
        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        Timber.d("tileRequest")
        Timber.d("viewmodel states: ${viewModel.states()}")
        Timber.d("viewmodel states value: ${viewModel.states().value}")
        val smokeCount = viewModel.states().value.smokesPerDay ?: 0

        return if (smokeCount >= 0) {
            Timber.d("tileRequest: smokeCount: $smokeCount")
            val layout = createTileLayout(smokeCount)
            TileBuilders.Tile.Builder()
                .setResourcesVersion(RESOURCES_VERSION)
                .setTileTimeline(
                    TimelineBuilders.Timeline.Builder()
                        .addTimelineEntry(
                            TimelineBuilders.TimelineEntry.Builder()
                                .setLayout(layout)
                                .build()
                        )
                        .build()
                )
                .build()
        } else {
            Timber.d("tileRequest: empty tile")
            createEmptyTile()
        }
    }

    private fun createTileLayout(smokeCount: Int): LayoutElementBuilders.Layout {
        Timber.d("createTileLayout: smokeCount: $smokeCount")
        val deviceParameters = DeviceParametersBuilders.DeviceParameters.Builder()
            .setScreenWidthDp(resources.configuration.screenWidthDp)
            .setScreenHeightDp(resources.configuration.screenHeightDp)
            .build()

        val content = PrimaryLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .setContent(
                Text.Builder(this, "Puchos hoy: $smokeCount")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(androidx.wear.protolayout.ColorBuilders.argb(0xFF006A6A.toInt()))
                    .build()
            )
            .build()

        return LayoutElementBuilders.Layout.Builder()
            .setRoot(content)
            .build()
    }

    private fun createEmptyTile(): TileBuilders.Tile {
        Timber.d("createEmptyTile")
        val layout = LayoutElementBuilders.Layout.Builder().build()
        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(layout)
                            .build()
                    )
                    .build()
            )
            .build()
    }

}

class DefaultTileUpdateRequester(private val context: Context) : TileUpdateRequester {

    override fun requestUpdate(tileService: Class<out TileService>) {
        val intent = Intent(context, tileService).apply {
            action = TileService.ACTION_BIND_TILE_PROVIDER
        }
        context.sendBroadcast(intent)
    }
}
