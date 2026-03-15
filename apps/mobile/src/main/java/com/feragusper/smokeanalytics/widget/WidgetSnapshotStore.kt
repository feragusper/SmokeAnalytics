package com.feragusper.smokeanalytics.widget

import android.content.Context
import com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetSnapshot

internal object WidgetSnapshotStore {

    private const val PREFS_NAME = "smoke_widget_snapshot"
    private const val KEY_TODAY = "today"
    private const val KEY_WEEK = "week"
    private const val KEY_MONTH = "month"
    private const val KEY_STREAK = "streak"
    private const val KEY_ELAPSED_HOURS = "elapsed_hours"
    private const val KEY_ELAPSED_MINUTES = "elapsed_minutes"
    private const val KEY_SPENT_TODAY = "spent_today"

    fun read(context: Context): WidgetSnapshot {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return WidgetSnapshot(
            todayCount = prefs.getInt(KEY_TODAY, 0),
            weekCount = prefs.getInt(KEY_WEEK, 0),
            monthCount = prefs.getInt(KEY_MONTH, 0),
            currentStreakHours = prefs.getLong(KEY_STREAK, 0L),
            elapsedHours = prefs.getLong(KEY_ELAPSED_HOURS, 0L),
            elapsedMinutes = prefs.getLong(KEY_ELAPSED_MINUTES, 0L),
            spentToday = prefs.getFloat(KEY_SPENT_TODAY, 0f).toDouble(),
        )
    }

    fun write(context: Context, snapshot: WidgetSnapshot) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_TODAY, snapshot.todayCount)
            .putInt(KEY_WEEK, snapshot.weekCount)
            .putInt(KEY_MONTH, snapshot.monthCount)
            .putLong(KEY_STREAK, snapshot.currentStreakHours)
            .putLong(KEY_ELAPSED_HOURS, snapshot.elapsedHours)
            .putLong(KEY_ELAPSED_MINUTES, snapshot.elapsedMinutes)
            .putFloat(KEY_SPENT_TODAY, snapshot.spentToday.toFloat())
            .apply()
    }
}
