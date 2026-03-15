package com.feragusper.smokeanalytics.libraries.preferences.domain

class FetchUserPreferencesUseCase(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(): UserPreferences = repository.fetch()
}
