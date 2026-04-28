package com.feragusper.smokeanalytics.tile

import android.annotation.SuppressLint
import android.content.Context
import com.feragusper.smokeanalytics.libraries.architecture.common.coroutines.DispatcherProviderImpl
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator
import com.feragusper.smokeanalytics.libraries.wear.data.WearSyncManagerImpl
import com.feragusper.smokeanalytics.libraries.wear.domain.WearSyncManager
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

// ViewModel for the Tile feature, responsible for managing the state and handling intents
object TileViewModel : MVIViewModel<TileIntent, TileViewState, TileResult, MVINavigator>(
    initialState = TileViewState()
) {

    // Lazy initialization for the WearSyncManager
    @SuppressLint("StaticFieldLeak")
    private lateinit var wearSyncManager: WearSyncManager.Wear
    private var isListeningForData = false

    // The TileProcessHolder is responsible for handling the intents for the tile
    private lateinit var processHolder: TileProcessHolder

    // Navigator to handle navigation actions
    override lateinit var navigator: MVINavigator

    // Initialize the WearSyncManager with the given context
    fun initialize(context: Context) {
        wearSyncManager = WearSyncManagerImpl(
            context = context.applicationContext,
            dispatcherProvider = DispatcherProviderImpl()
        ).Wear()
        processHolder = TileProcessHolder(wearSyncManager)
        if (!isListeningForData) {
            isListeningForData = true
            intents().trySend(TileIntent.FetchSmokes)
        }
    }

    init {
        Timber.d("TileViewModel initialized")
    }

    // Transformer function to process the TileIntent and return the corresponding TileResult
    override fun transformer(intent: TileIntent): Flow<TileResult> =
        processHolder.processIntent(intent)

    // Reducer function to update the state based on the TileResult
    override fun reducer(
        previous: TileViewState,
        result: TileResult
    ): TileViewState = when (result) {
        is TileResult.AddSmokeStarted -> previous.copy(
            addSmokePendingSinceMillis = result.requestedAtMillis,
            error = null,
        )

        is TileResult.FetchSmokesSuccess -> previous.copy(
            todayCount = result.todayCount,
            targetGapMinutes = result.targetGapMinutes,
            averageSmokesPerDayWeek = result.averageSmokesPerDayWeek,
            lastSmokeTimestamp = result.lastSmokeTimestamp,
            addSmokePendingSinceMillis = previous.addSmokePendingSinceMillis
                ?.takeIf { previous.shouldKeepAddSmokePending(result) },
            error = null,
        )

        is TileResult.AddSmokeRequestSent -> previous.copy()

        is TileResult.Error -> previous.copy(
            addSmokePendingSinceMillis = null,
            error = result,
        )
    }

    private fun TileViewState.shouldKeepAddSmokePending(result: TileResult.FetchSmokesSuccess): Boolean {
        val pendingSince = addSmokePendingSinceMillis ?: return false
        val todayCountAdvanced = todayCount != null && result.todayCount > todayCount
        val timestampAcknowledged = result.lastSmokeTimestamp != null &&
            result.lastSmokeTimestamp >= pendingSince - ADD_SMOKE_CLOCK_SKEW_TOLERANCE_MILLIS

        return !todayCountAdvanced && !timestampAcknowledged
    }

    private const val ADD_SMOKE_CLOCK_SKEW_TOLERANCE_MILLIS = 30_000L
}
