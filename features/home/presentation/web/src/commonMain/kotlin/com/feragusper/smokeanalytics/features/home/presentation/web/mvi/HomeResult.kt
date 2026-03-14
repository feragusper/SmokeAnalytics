package com.feragusper.smokeanalytics.features.home.presentation.web.mvi

import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult
import com.feragusper.smokeanalytics.features.home.domain.FinancialSummary
import com.feragusper.smokeanalytics.features.home.domain.GamificationSummary
import com.feragusper.smokeanalytics.features.home.domain.GreetingState
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences

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
        val smokeCountListResult: SmokeCountListResult,
        val preferences: UserPreferences,
        val greetingState: GreetingState,
        val financialSummary: FinancialSummary,
        val gamificationSummary: GamificationSummary,
    ) : HomeResult

    data object FetchSmokesError : HomeResult

    data class UpdateTimeSinceLastCigarette(
        val timeSinceLastCigarette: Pair<Long, Long>
    ) : HomeResult
}
