package com.feragusper.smokeanalytics.widget

import android.content.Context
import com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetSnapshot

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
}
