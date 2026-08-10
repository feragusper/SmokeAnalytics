package com.feragusper.smokeanalytics.libraries.preferences.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.DataSource

class FetchUserPreferencesUseCase(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(
        source: DataSource = DataSource.DEFAULT,
    ): UserPreferences = repository.fetch(source)
}
