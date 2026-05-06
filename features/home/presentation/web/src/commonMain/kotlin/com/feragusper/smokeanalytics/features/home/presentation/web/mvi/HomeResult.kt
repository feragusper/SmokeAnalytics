package com.feragusper.smokeanalytics.features.home.presentation.web.mvi

import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult
import com.feragusper.smokeanalytics.features.home.domain.FinancialSummary
import com.feragusper.smokeanalytics.features.home.domain.GamificationSummary
import com.feragusper.smokeanalytics.features.home.domain.GreetingState
import com.feragusper.smokeanalytics.features.home.domain.RateSummary
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationTrackingAvailability
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke

sealed interface HomeResult {

    data object Loading : HomeResult

    data object RefreshLoading : HomeResult

    data object NotLoggedIn : HomeResult

    data object GoToAuthentication : HomeResult

    data object GoToHistory : HomeResult

    data object GoToGoals : HomeResult

    data object AddSmokeSuccess : HomeResult

    data object StartNewDaySuccess : HomeResult

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
        val rateSummary: RateSummary,
        val gamificationSummary: GamificationSummary,
        val goalProgress: GoalProgress?,
        val canStartNewDay: Boolean,
        val locationTrackingAvailability: LocationTrackingAvailability,
    ) : HomeResult

    data object FetchSmokesError : HomeResult

    data class UpdateTimeSinceLastCigarette(
        val timeSinceLastCigarette: Pair<Long, Long>,
        val lastSmoke: Smoke?,
    ) : HomeResult
}
