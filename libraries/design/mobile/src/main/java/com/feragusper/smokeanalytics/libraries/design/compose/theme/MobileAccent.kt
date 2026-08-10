package com.feragusper.smokeanalytics.libraries.design.compose.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * User-selectable accent for the app's primary color. [Default] keeps the built-in scheme; the
 * others override `primary`/`onPrimary`.
 *
 * Each accent carries a light and a dark variant: a mid-tone primary reads well on the light
 * scheme's light surfaces, but the same tone on a dark surface is muddy and low-contrast. Following
 * the Material tonal convention, the dark variant is a lighter, desaturated primary paired with a
 * dark `onPrimary`. Kept local to the device (like the web accent) — appearance, not synced content.
 */
enum class MobileAccent(
    val id: String,
    val label: String,
    val lightPrimary: Color?,
    val lightOnPrimary: Color?,
    val darkPrimary: Color?,
    val darkOnPrimary: Color?,
) {
    Default("default", "Default", null, null, null, null),
    Teal("teal", "Teal", Color(0xFF006A6A), Color.White, Color(0xFF4FD8D8), Color(0xFF003737)),
    Indigo("indigo", "Indigo", Color(0xFF4C56C0), Color.White, Color(0xFFBBC3FF), Color(0xFF1B2678)),
    Rose("rose", "Rose", Color(0xFFB23A6B), Color.White, Color(0xFFFFB0CC), Color(0xFF66052E)),
    Amber("amber", "Amber", Color(0xFF9A6400), Color.White, Color(0xFFFFB951), Color(0xFF522300)),
    Forest("forest", "Forest", Color(0xFF2E7D46), Color.White, Color(0xFF7EDB94), Color(0xFF00391B));

    /** Primary override for the given mode, or null when this accent leaves the scheme untouched. */
    fun primary(darkTheme: Boolean): Color? = if (darkTheme) darkPrimary else lightPrimary

    /** onPrimary override paired with [primary] for the given mode. */
    fun onPrimary(darkTheme: Boolean): Color? = if (darkTheme) darkOnPrimary else lightOnPrimary
}

/**
 * Holds the current accent as observable Compose state, backed by SharedPreferences so it reads
 * synchronously at cold start (no theme flash). Load once from the Application, update from Settings.
 */
object AccentHolder {
    private const val PREFS = "sa_appearance"
    private const val KEY = "accent"

    var current by mutableStateOf(MobileAccent.Default)
        private set

    fun load(context: Context) {
        val id = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null)
        current = MobileAccent.entries.firstOrNull { it.id == id } ?: MobileAccent.Default
    }

    fun set(context: Context, accent: MobileAccent) {
        current = accent
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, accent.id)
            .apply()
    }
}
