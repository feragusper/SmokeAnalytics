package com.feragusper.smokeanalytics.libraries.design.compose.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * User-selectable accent for the app's primary color. [Default] keeps the built-in scheme; the
 * others override `primary`/`onPrimary`. Kept local to the device (like the web accent) — it's an
 * appearance preference, not synced content.
 */
enum class MobileAccent(
    val id: String,
    val label: String,
    val primary: Color?,
    val onPrimary: Color?,
) {
    Default("default", "Default", null, null),
    Teal("teal", "Teal", Color(0xFF006A6A), Color.White),
    Indigo("indigo", "Indigo", Color(0xFF4C56C0), Color.White),
    Rose("rose", "Rose", Color(0xFFB23A6B), Color.White),
    Amber("amber", "Amber", Color(0xFF9A6400), Color.White),
    Forest("forest", "Forest", Color(0xFF2E7D46), Color.White),
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
