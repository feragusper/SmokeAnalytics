package com.feragusper.smokeanalytics.features.settings.presentation

import androidx.annotation.StringRes

/** The settings sub-screens reachable as their own full-screen destinations. */
enum class SettingsSection(@StringRes val titleRes: Int) {
    PREFERENCES(R.string.settings_preferences),
    PERSONALIZATION(R.string.settings_make_it_yours),
    TRIGGERS(R.string.settings_manage_triggers),
    APPEARANCE(R.string.settings_appearance),
    ABOUT(R.string.settings_about_support),
}
