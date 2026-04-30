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
        is TileResult.AddSmokeStarted -> previous.withOptimisticAddSmoke(result.requestedAtMillis)

        is TileResult.FetchSmokesSuccess -> previous.updatedWithSmokeSnapshot(result)

        is TileResult.AddSmokeRequestSent -> previous.copy()

        is TileResult.Error -> previous.withPendingAddSmokeRolledBack()
    }

    private fun TileViewState.updatedWithSmokeSnapshot(result: TileResult.FetchSmokesSuccess): TileViewState {
        val keepPending = shouldKeepAddSmokePending(result)
        if (keepPending) return copy(error = null)

        return copy(
            todayCount = result.todayCount,
            targetGapMinutes = result.targetGapMinutes,
            averageSmokesPerDayWeek = result.averageSmokesPerDayWeek,
            lastSmokeTimestamp = result.lastSmokeTimestamp,
            addSmokePendingCount = 0,
            addSmokePendingBaseline = null,
            error = null,
        )
    }

    private fun TileViewState.shouldKeepAddSmokePending(result: TileResult.FetchSmokesSuccess): Boolean {
        if (addSmokePendingCount <= 0) return false
        val baseline = addSmokePendingBaseline ?: return false
        val baselineCount = baseline.todayCount ?: 0
        val expectedTodayCount = baselineCount + addSmokePendingCount
        val todayCountAcknowledged = result.todayCount >= expectedTodayCount
        val pendingSince = lastSmokeTimestamp ?: return false
        val timestampAcknowledged = result.lastSmokeTimestamp != null &&
            result.lastSmokeTimestamp >= pendingSince - ADD_SMOKE_CLOCK_SKEW_TOLERANCE_MILLIS

        return !todayCountAcknowledged && !timestampAcknowledged
    }

    private const val ADD_SMOKE_CLOCK_SKEW_TOLERANCE_MILLIS = 30_000L
}
