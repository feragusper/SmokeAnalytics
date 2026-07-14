package com.feragusper.smokeanalytics.features.goals.presentation.process
import com.feragusper.smokeanalytics.libraries.architecture.domain.NoOpAnalyticsTracker

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.goals.domain.EvaluateGoalProgressUseCase
import com.feragusper.smokeanalytics.features.goals.domain.GoalStatus
import com.feragusper.smokeanalytics.features.goals.presentation.mvi.GoalsIntent
import com.feragusper.smokeanalytics.features.goals.presentation.mvi.GoalsResult
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsProcessHolderTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var processHolder: GoalsProcessHolder

    private val fetchSessionUseCase: FetchSessionUseCase = mockk()
    private val fetchUserPreferencesUseCase: FetchUserPreferencesUseCase = mockk()
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase = mockk()
    private val fetchSmokesUseCase: FetchSmokesUseCase = mockk()
    // Use real instance — pure function, no side effects
    private val evaluateGoalProgressUseCase = EvaluateGoalProgressUseCase()

    private val defaultPreferences = UserPreferences()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        processHolder = GoalsProcessHolder(
            fetchSessionUseCase = fetchSessionUseCase,
            fetchUserPreferencesUseCase = fetchUserPreferencesUseCase,
            updateUserPreferencesUseCase = updateUserPreferencesUseCase,
            fetchSmokesUseCase = fetchSmokesUseCase,
            analyticsTracker = NoOpAnalyticsTracker,
            evaluateGoalProgressUseCase = evaluateGoalProgressUseCase,
        )

        coEvery { fetchUserPreferencesUseCase() } returns defaultPreferences
        coEvery { fetchSmokesUseCase(any(), any()) } returns emptyList()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    @DisplayName("GIVEN user is logged in")
    inner class UserIsLoggedIn {

        private val loggedInSession = mockk<Session.LoggedIn> {
            coEvery { user } returns mockk {
                coEvery { email } returns "test@example.com"
            }
        }

        @BeforeEach
        fun setUp() {
            coEvery { fetchSessionUseCase() } returns loggedInSession
        }

        @Test
        fun `WHEN fetching goals THEN it emits Loading then Loaded`() = runTest {
            processHolder.processIntent(GoalsIntent.FetchGoals).test {
                awaitItem() shouldBeEqualTo GoalsResult.Loading
                val loaded = awaitItem()
                loaded.shouldBeInstanceOf<GoalsResult.Loaded>()
                (loaded as GoalsResult.Loaded).email shouldBeEqualTo "test@example.com"
                loaded.preferences shouldBeEqualTo defaultPreferences
                awaitComplete()
            }
        }

        @Test
        fun `WHEN saving a goal THEN it updates preferences and emits Loaded then GoalSaved`() = runTest {
            val goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 5)
            coEvery { updateUserPreferencesUseCase(any()) } just Runs

            processHolder.processIntent(GoalsIntent.SaveGoal(goal)).test {
                awaitItem() shouldBeEqualTo GoalsResult.Loading
                val loaded = awaitItem()
                loaded.shouldBeInstanceOf<GoalsResult.Loaded>()
                awaitItem() shouldBeEqualTo GoalsResult.GoalSaved
                coVerify(exactly = 1) {
                    updateUserPreferencesUseCase(defaultPreferences.copy(activeGoal = goal))
                }
                awaitComplete()
            }
        }

        @Test
        fun `WHEN clearing a goal THEN it updates preferences with null goal`() = runTest {
            coEvery { updateUserPreferencesUseCase(any()) } just Runs

            processHolder.processIntent(GoalsIntent.ClearGoal).test {
                awaitItem() shouldBeEqualTo GoalsResult.Loading
                val loaded = awaitItem()
                loaded.shouldBeInstanceOf<GoalsResult.Loaded>()
                awaitItem() shouldBeEqualTo GoalsResult.GoalSaved
                coVerify(exactly = 1) {
                    updateUserPreferencesUseCase(defaultPreferences.copy(activeGoal = null))
                }
                awaitComplete()
            }
        }

        @Test
        fun `WHEN fetching goals with active goal THEN goal progress is included`() = runTest {
            val goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 5)
            coEvery { fetchUserPreferencesUseCase() } returns defaultPreferences.copy(activeGoal = goal)

            processHolder.processIntent(GoalsIntent.FetchGoals).test {
                awaitItem() shouldBeEqualTo GoalsResult.Loading
                val loaded = awaitItem() as GoalsResult.Loaded
                loaded.goalProgress.shouldNotBeNull()
                loaded.goalProgress!!.status shouldBeEqualTo GoalStatus.OnTrack
                awaitComplete()
            }
        }

        @Test
        fun `WHEN saving goal fails THEN it emits Error`() = runTest {
            coEvery { updateUserPreferencesUseCase(any()) } throws IllegalStateException("Network error")

            processHolder.processIntent(GoalsIntent.SaveGoal(SmokingGoal.DailyCap(maxCigarettesPerDay = 5))).test {
                awaitItem() shouldBeEqualTo GoalsResult.Loading
                val error = awaitItem()
                error.shouldBeInstanceOf<GoalsResult.Error>()
                awaitComplete()
            }
        }

        @Test
        fun `WHEN fetching goals fails THEN it emits Error`() = runTest {
            coEvery { fetchUserPreferencesUseCase() } throws RuntimeException("Firestore down")

            processHolder.processIntent(GoalsIntent.FetchGoals).test {
                awaitItem() shouldBeEqualTo GoalsResult.Loading
                val error = awaitItem()
                error.shouldBeInstanceOf<GoalsResult.Error>()
                awaitComplete()
            }
        }
    }

    @Nested
    @DisplayName("GIVEN user is not logged in")
    inner class UserIsNotLoggedIn {

        @BeforeEach
        fun setUp() {
            coEvery { fetchSessionUseCase() } returns mockk<Session.Anonymous>()
        }

        @Test
        fun `WHEN fetching goals THEN it emits LoggedOut`() = runTest {
            processHolder.processIntent(GoalsIntent.FetchGoals).test {
                awaitItem() shouldBeEqualTo GoalsResult.Loading
                awaitItem() shouldBeEqualTo GoalsResult.LoggedOut
                awaitComplete()
            }
        }
    }
}
