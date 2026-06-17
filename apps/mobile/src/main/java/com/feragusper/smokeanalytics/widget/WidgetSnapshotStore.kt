package com.feragusper.smokeanalytics.widget

import android.content.Context
import com.feragusper.smokeanalytics.features.goals.domain.EvaluateGoalProgressUseCase
import com.feragusper.smokeanalytics.features.goals.domain.goalDataFetchStart
import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.domain.toWidgetSnapshot
import com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetSnapshot
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import org.koin.mp.KoinPlatform.getKoin

internal object WidgetSnapshotStore {

    private const val PREFS_NAME = "smoke_widget_snapshot"
    private const val KEY_TODAY = "today"
    private const val KEY_ELAPSED_HOURS = "elapsed_hours"
    private const val KEY_ELAPSED_MINUTES = "elapsed_minutes"
    private const val KEY_TARGET_GAP_MINUTES = "target_gap_minutes"
    private const val KEY_AVERAGE_SMOKES_PER_DAY_WEEK = "average_smokes_per_day_week"

    fun read(context: Context): WidgetSnapshot {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return WidgetSnapshot(
            todayCount = prefs.getInt(KEY_TODAY, 0),
            elapsedHours = prefs.getLong(KEY_ELAPSED_HOURS, 0L),
            elapsedMinutes = prefs.getLong(KEY_ELAPSED_MINUTES, 0L),
            targetGapMinutes = prefs.getInt(KEY_TARGET_GAP_MINUTES, 0),
            averageSmokesPerDayWeek = prefs.getFloat(KEY_AVERAGE_SMOKES_PER_DAY_WEEK, 0f).toDouble(),
        )
    }

    suspend fun readFreshOrStored(context: Context): WidgetSnapshot =
        runCatching { readFresh(context) }
            .onSuccess { write(context, it) }
            .getOrElse { read(context) }

    fun write(context: Context, snapshot: WidgetSnapshot) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_TODAY, snapshot.todayCount)
            .putLong(KEY_ELAPSED_HOURS, snapshot.elapsedHours)
            .putLong(KEY_ELAPSED_MINUTES, snapshot.elapsedMinutes)
            .putInt(KEY_TARGET_GAP_MINUTES, snapshot.targetGapMinutes)
            .putFloat(KEY_AVERAGE_SMOKES_PER_DAY_WEEK, snapshot.averageSmokesPerDayWeek.toFloat())
            .apply()
    }

    private suspend fun readFresh(context: Context): WidgetSnapshot {
        val koin = getKoin()
        val preferences = koin.get<FetchUserPreferencesUseCase>().invoke()
        val smokeCounts = koin.get<FetchSmokeCountListUseCase>().invoke(
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val goalSmokes = koin.get<FetchSmokesUseCase>().invoke(start = goalDataFetchStart(preferences))
        val goalProgress = koin.get<EvaluateGoalProgressUseCase>().invoke(
            activeGoal = preferences.activeGoal,
            smokes = goalSmokes,
            preferences = preferences,
        )
        return smokeCounts.toWidgetSnapshot(preferences, goalProgress)
    }
}
