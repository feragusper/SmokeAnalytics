package com.feragusper.smokeanalytics.libraries.design.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.browser.localStorage
import kotlinx.browser.window

/** Languages the web app can render in. [code] matches the persisted value and `navigator.language`. */
enum class AppLanguage(val code: String, val nativeLabel: String) {
    EN("en", "English"),
    ES("es", "Español");

    companion object {
        private const val STORAGE_KEY = "sa.lang"

        /** Persisted choice, else the browser's language, else English. */
        fun initial(): AppLanguage {
            val stored = runCatching { localStorage.getItem(STORAGE_KEY) }.getOrNull()
            stored?.let { code -> entries.firstOrNull { it.code == code } }?.let { return it }
            val browser = runCatching { window.navigator.language.lowercase() }.getOrNull().orEmpty()
            return if (browser.startsWith("es")) ES else EN
        }

        fun persist(language: AppLanguage) {
            runCatching { localStorage.setItem(STORAGE_KEY, language.code) }
        }
    }
}

/** Current UI strings, provided at the app root and swapped when the language changes. */
val LocalStrings = staticCompositionLocalOf<AppStrings> { EnglishStrings }

/** Current language, provided alongside [LocalStrings] so widgets can render a selector. */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.EN }
