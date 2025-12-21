package com.feragusper.smokeanalytics.features.home.presentation.web

import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult

sealed interface HomeResult {

    data object Loading : HomeResult

    data object RefreshLoading : HomeResult

    data object NotLoggedIn : HomeResult

    data object GoToAuthentication : HomeResult

    data object GoToHistory : HomeResult

    data object AddSmokeSuccess : HomeResult

    data object EditSmokeSuccess : HomeResult

    data object DeleteSmokeSuccess : HomeResult

    sealed interface Error : HomeResult {
        data object Generic : Error
        data object NotLoggedIn : Error
    }

    data class FetchSmokesSuccess(
        val smokeCountListResult: SmokeCountListResult
    ) : HomeResult

    data object FetchSmokesError : HomeResult

    data class UpdateTimeSinceLastCigarette(
        val timeSinceLastCigarette: Pair<Long, Long>
    ) : HomeResult
}