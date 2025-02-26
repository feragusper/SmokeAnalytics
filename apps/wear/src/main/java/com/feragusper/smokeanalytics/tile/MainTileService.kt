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
import com.feragusper.smokeanalytics.R
import com.feragusper.smokeanalytics.libraries.architecture.presentation.BuildConfig
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

// The main Tile service for the wearable device
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    // ViewModel for the tile UI state management
    private val viewModel = TileViewModel.apply {
        initialize(this@MainTileService)
    }

    // Tile update requester to request updates
    private val tileUpdateRequester: TileUpdateRequester by lazy {
        getUpdater(this)
    }

    // Called when the service is created
    override fun onCreate() {
        super.onCreate()

        // Debugging setup for Timber logging in debug mode
        if (BuildConfig.DEBUG && Timber.forest().isEmpty()) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("onCreate")

        // Observe the state changes and request a tile update on state changes
        lifecycleScope.launch {
            viewModel.states()
                .distinctUntilChanged { old, new -> old.smokesPerDay == new.smokesPerDay }
                .collect {
                    Timber.d("onCreate: collect: $it")
                    tileUpdateRequester.requestUpdate(MainTileService::class.java)
                }
        }
    }

    // Handles tile request to render the tile UI based on current state
    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val state = viewModel.states().value

        // Handle action when "add_smoke_action" is clicked
        if (requestParams.currentState.lastClickableId == ADD_SMOKE_ACTION_ID) {
            Timber.d("Add Smoke clicked! Updating ViewModel...")
            viewModel.intents().trySend(TileIntent.AddSmoke)
        }

        // Return the tile based on the state
        return createTile(state)
    }

    // Handles resource requests to load the resources used in the tile UI
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

    // Creates the tile using the current state
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

    // Creates the layout of the tile with the current state
    private fun createTileLayout(state: TileViewState): LayoutElementBuilders.Layout {
        val deviceParameters = DeviceParametersBuilders.DeviceParameters.Builder()
            .setScreenWidthDp(resources.configuration.screenWidthDp)
            .setScreenHeightDp(resources.configuration.screenHeightDp)
            .build()

        // Format last smoke time
        val lastSmokeText = formatLastSmokeTime(state.lastSmokeTimestamp)

        // Create text elements for displaying statistics and last smoke time
        val statsText = Text.Builder(this, getString(R.string.stats_today, state.smokesPerDay ?: 0))
            .setTypography(Typography.TYPOGRAPHY_TITLE3)
            .setColor(androidx.wear.protolayout.ColorBuilders.argb(0xFF00897B.toInt()))
            .setMaxLines(1)
            .build()

        val lastSmoke = Text.Builder(this, lastSmokeText)
            .setTypography(Typography.TYPOGRAPHY_BODY2)
            .setColor(androidx.wear.protolayout.ColorBuilders.argb(0xFF00897B.toInt()))
            .setMaxLines(1)
            .build()

        // Create the "Add Smoke" chip button
        val addSmokeChip = CompactChip.Builder(
            this,
            androidx.wear.protolayout.ModifiersBuilders.Clickable.Builder()
                .setId(ADD_SMOKE_ACTION_ID)
                .setOnClick(ActionBuilders.LoadAction.Builder().build())
                .build(),
            deviceParameters
        )
            .setIconContent("smoke_icon")
            .setChipColors(ChipDefaults.PRIMARY_COLORS)
            .build()

        // Build the layout using the created elements
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

    // Formats the last smoke time into a human-readable format
    private fun formatLastSmokeTime(timestamp: Long?): String {
        if (timestamp == null || timestamp == 0L) return getString(R.string.last_smoke_na)

        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> getString(R.string.last_smoke_just_now)
            diff < TimeUnit.HOURS.toMillis(1) -> getString(
                R.string.last_smoke_minutes_ago,
                TimeUnit.MILLISECONDS.toMinutes(diff)
            )

            diff < TimeUnit.DAYS.toMillis(1) -> getString(
                R.string.last_smoke_hours_ago,
                TimeUnit.MILLISECONDS.toHours(diff)
            )

            else -> getString(R.string.last_smoke_days_ago, TimeUnit.MILLISECONDS.toDays(diff))
        }
    }

    companion object {
        private const val ADD_SMOKE_ACTION_ID = "add_smoke_action"
        private const val RESOURCES_VERSION = "1"
    }

}