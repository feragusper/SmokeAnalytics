package com.feragusper.smokeanalytics.libraries.design.theme

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.HTMLElement

/**
 * User-selectable accent color for the web app. Only overrides the primary token; the rest of
 * the palette stays consistent. Persisted in localStorage (like the language) so it applies
 * pre-login and across reloads without waiting on synced preferences.
 */
enum class AccentColor(val id: String, val label: String, val primary: String, val onPrimary: String) {
    Teal("teal", "Teal", "#006A6A", "#FFFFFF"),
    Indigo("indigo", "Indigo", "#4C56C0", "#FFFFFF"),
    Rose("rose", "Rose", "#B23A6B", "#FFFFFF"),
    Amber("amber", "Amber", "#9A6400", "#FFFFFF"),
    Forest("forest", "Forest", "#2E7D46", "#FFFFFF");

    companion object {
        private const val STORAGE_KEY = "sa.accent"
        val Default = Teal

        fun initial(): AccentColor {
            val stored = runCatching { localStorage.getItem(STORAGE_KEY) }.getOrNull()
            return entries.firstOrNull { it.id == stored } ?: Default
        }

        fun persist(accent: AccentColor) {
            runCatching { localStorage.setItem(STORAGE_KEY, accent.id) }
        }
    }
}

/** Applies (or clears) the accent override on the document root so every `var(--sa-color-primary)` follows. */
fun applyAccent(accent: AccentColor) {
    val root = document.documentElement as? HTMLElement ?: return
    root.style.setProperty("--sa-color-primary", accent.primary)
    root.style.setProperty("--sa-color-onPrimary", accent.onPrimary)
}

val LocalAccentColor = staticCompositionLocalOf { AccentColor.Default }
