package com.feragusper.smokeanalytics.libraries.design.compose.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * User-selectable app theme. [System] follows the device's light/dark setting (default); the
 * others force a fixed appearance. Kept local to the device (like [MobileAccent]) — it's an
 * appearance preference, not synced content.
 */
enum class ThemeMode(val id: String) {
    System("system"),
    Light("light"),
    Dark("dark"),
}

/**
 * Resolves whether the dark color scheme should apply for this mode. [ThemeMode.System] defers to
 * [isSystemInDarkTheme]; the others are fixed.
 */
@Composable
fun ThemeMode.resolveDarkTheme(): Boolean = when (this) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

/**
 * Holds the current theme mode as observable Compose state, backed by SharedPreferences so it reads
 * synchronously at cold start (no theme flash). Load once from the Application, update from Settings.
 * Reading [current] inside a composable subscribes it, so changing the mode re-themes the app live.
 */
object ThemeModeHolder {
    private const val PREFS = "sa_appearance"
    private const val KEY = "theme_mode"

    var current by mutableStateOf(ThemeMode.System)
        private set

    fun load(context: Context) {
        val id = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null)
        current = ThemeMode.entries.firstOrNull { it.id == id } ?: ThemeMode.System
    }

    fun set(context: Context, mode: ThemeMode) {
        current = mode
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, mode.id)
            .apply()
    }
}
