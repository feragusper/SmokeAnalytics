package com.feragusper.smokeanalytics.widget

import android.content.Context
import com.feragusper.smokeanalytics.features.goals.domain.goalDataFetchStart
import com.feragusper.smokeanalytics.features.home.domain.toWidgetSnapshot
import com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetSnapshot
import dagger.hilt.android.EntryPointAccessors

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
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            HomeStatusWidgetEntryPoint::class.java,
        )
        val preferences = entryPoint.fetchUserPreferencesUseCase().invoke()
        val smokeCounts = entryPoint.fetchSmokeCountListUseCase().invoke(
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val goalSmokes = entryPoint.fetchSmokesUseCase().invoke(start = goalDataFetchStart(preferences))
        val goalProgress = entryPoint.evaluateGoalProgressUseCase().invoke(
            activeGoal = preferences.activeGoal,
            smokes = goalSmokes,
            preferences = preferences,
        )
        return smokeCounts.toWidgetSnapshot(preferences, goalProgress)
    }
}
