package com.feragusper.smokeanalytics.libraries.preferences.domain

interface UserPreferencesRepository {
    suspend fun fetch(): UserPreferences
    suspend fun update(preferences: UserPreferences)
}
