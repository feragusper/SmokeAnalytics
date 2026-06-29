package com.feragusper.smokeanalytics.features.home.presentation.mvi

import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult
import com.feragusper.smokeanalytics.features.home.domain.FinancialSummary
import com.feragusper.smokeanalytics.features.home.domain.GamificationSummary
import com.feragusper.smokeanalytics.features.home.domain.GreetingState
import com.feragusper.smokeanalytics.features.home.domain.RateSummary
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationTrackingAvailability
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.Craving
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingStats
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.TriggerOption

/**
 * Represents the possible outcomes of processing [HomeIntent] actions.
 *
 * This sealed interface defines all possible states the Home module can be in,
 * based on the result of processing a user intent.
 */
sealed interface HomeResult : MVIResult {

    /**
     * Indicates that the application is currently loading or processing data.
     * This is typically used to show a loading indicator.
     */
    data object Loading : HomeResult

    /**
     * Indicates that a refresh operation is in progress.
     */
    data object RefreshLoading : HomeResult

    /**
     * Indicates that the user is not logged in.
     */
    data object NotLoggedIn : HomeResult

    /**
     * Triggers navigation to the authentication screen.
     */
    data object GoToAuthentication : HomeResult

    /**
     * Triggers navigation to the smoke history screen.
     */
    data object GoToHistory : HomeResult

    data object GoToGoals : HomeResult

    /**
     * Indicates that a smoke event was successfully added.
     *
     * @property smokeId The id of the new smoke, used to open the relationship prompt.
     */
    data class AddSmokeSuccess(val smokeId: String) : HomeResult

    /**
     * A smoke's relationship was saved or skipped; the home should refetch and close the prompt.
     */
    data object RelationshipUpdated : HomeResult

    /**
     * The relationship prompt was dismissed without answering.
     */
    data object RelationshipPromptDismissed : HomeResult

    /**
     * Indicates that the current day was manually restarted.
     */
    data object StartNewDaySuccess : HomeResult

    /**
     * Indicates that a smoke event was successfully edited.
     */
    data object EditSmokeSuccess : HomeResult

    /**
     * Indicates that a smoke event was successfully deleted.
     */
    data object DeleteSmokeSuccess : HomeResult

    /**
     * Represents errors that might occur during the processing of home intents.
     */
    sealed interface Error : HomeResult {
        /**
         * A generic error result.
         */
        data class Generic(
            val debugMessage: String? = null,
        ) : Error

        /**
         * Error indicating that the user is not logged in.
         */
        data object NotLoggedIn : Error
    }

    /**
     * Indicates a successful fetch of smoke data, containing smoke counts and latest smokes.
     *
     * @property smokeCountListResult The result containing counts of smokes and latest smokes.
     */
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
        val previousMonthCount: Int = 0,
        val activeCraving: Craving? = null,
        val cravingStats: CravingStats = CravingStats(),
        val pendingRelationshipSmokes: List<Smoke> = emptyList(),
        val availableTriggers: List<TriggerOption> = emptyList(),
    ) : HomeResult

    /**
     * A craving was tracked and the user should wait before smoking again.
     *
     * @property craving The pending craving (carries the target time for the countdown).
     */
    data class CravingTracked(val craving: Craving) : HomeResult

    /**
     * A craving was tracked but no wait is needed — it is already a good time.
     */
    data object CravingNoWaitNeeded : HomeResult

    /**
     * A craving was resolved.
     *
     * @property outcome How it ended.
     * @property points Points awarded.
     */
    data class CravingResolved(
        val outcome: CravingOutcome,
        val points: Int,
    ) : HomeResult

    /**
     * Dismisses the transient craving hint.
     */
    data object CravingHintDismissed : HomeResult

    /**
     * Dismisses the craving celebration.
     */
    data object CravingCelebrationDismissed : HomeResult

    /**
     * Updates the time elapsed since the last cigarette.
     *
     * @property timeSinceLastCigarette The time elapsed since the last smoke event,
     * represented as a pair of hours and minutes.
     */
    data class UpdateTimeSinceLastCigarette(
        val timeSinceLastCigarette: Pair<Long, Long>,
        val lastSmoke: Smoke?,
    ) : HomeResult
}
