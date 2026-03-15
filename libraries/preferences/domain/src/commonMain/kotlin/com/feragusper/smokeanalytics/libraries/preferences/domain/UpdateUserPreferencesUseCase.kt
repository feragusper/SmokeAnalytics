package com.feragusper.smokeanalytics.libraries.preferences.domain

class UpdateUserPreferencesUseCase(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(preferences: UserPreferences) {
        repository.update(preferences)
    }
}
