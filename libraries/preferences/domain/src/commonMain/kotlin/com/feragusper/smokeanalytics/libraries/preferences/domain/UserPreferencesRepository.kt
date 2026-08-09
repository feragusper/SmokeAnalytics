package com.feragusper.smokeanalytics.libraries.preferences.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.DataSource

interface UserPreferencesRepository {
    /**
     * Fetches the user's preferences.
     *
     * @param source Where to read from (cache-first vs server). Defaults to Firestore's default.
     */
    suspend fun fetch(source: DataSource = DataSource.DEFAULT): UserPreferences
    suspend fun update(preferences: UserPreferences)
}
