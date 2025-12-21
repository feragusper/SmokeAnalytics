package com.feragusper.smokeanalytics.features.home.presentation.web

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke

data class HomeViewState(
    val displayLoading: Boolean = false,
    val displayRefreshLoading: Boolean = false,
    val smokesPerDay: Int? = null,
    val smokesPerWeek: Int? = null,
    val smokesPerMonth: Int? = null,
    val timeSinceLastCigarette: Pair<Long, Long>? = null,
    val latestSmokes: List<Smoke>? = null,
    val error: HomeError? = null,
) {
    sealed interface HomeError {
        data object Generic : HomeError
        data object NotLoggedIn : HomeError
    }
}