package com.feragusper.smokeanalytics.features.goals.presentation

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.goals.domain.GoalStatus
import com.feragusper.smokeanalytics.features.goals.presentation.mvi.GoalsIntent
import com.feragusper.smokeanalytics.features.goals.presentation.mvi.GoalsResult
import com.feragusper.smokeanalytics.features.goals.presentation.process.GoalsProcessHolder
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsViewModelTest {

    private val processHolder: GoalsProcessHolder = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN loading result WHEN initial state THEN displayLoading is true`() = runTest {
        every { processHolder.processIntent(any()) } returns flowOf(GoalsResult.Loading)

        val viewModel = GoalsViewModel(processHolder)

        viewModel.states().test {
            awaitItem().displayLoading shouldBeEqualTo true
        }
    }

    @Test
    fun `GIVEN loaded result WHEN goals loaded THEN state has preferences and no loading`() = runTest {
        val preferences = UserPreferences()
        val goalProgress = GoalProgress(
            goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 5),
            status = GoalStatus.OnTrack,
        )

        every { processHolder.processIntent(any()) } returns flowOf(
            GoalsResult.Loaded(
                email = "test@example.com",
                preferences = preferences,
                goalProgress = goalProgress,
            )
        )

        val viewModel = GoalsViewModel(processHolder)

        viewModel.states().test {
            val state = awaitItem()
            state.displayLoading shouldBeEqualTo false
            state.currentEmail shouldBeEqualTo "test@example.com"
            state.goalProgress shouldBeEqualTo goalProgress
        }
    }

    @Test
    fun `GIVEN logged out result WHEN goals fetched THEN state reflects logged out`() = runTest {
        every { processHolder.processIntent(any()) } returns flowOf(GoalsResult.LoggedOut)

        val viewModel = GoalsViewModel(processHolder)

        viewModel.states().test {
            val state = awaitItem()
            state.displayLoading shouldBeEqualTo false
            state.currentEmail.shouldBeNull()
        }
    }

    @Test
    fun `GIVEN error result WHEN goals fetched THEN error message is set`() = runTest {
        every { processHolder.processIntent(any()) } returns flowOf(
            GoalsResult.Error("Something went wrong")
        )

        val viewModel = GoalsViewModel(processHolder)

        viewModel.states().test {
            val state = awaitItem()
            state.displayLoading shouldBeEqualTo false
            state.errorMessage shouldBeEqualTo "Something went wrong"
        }
    }

    @Test
    fun `GIVEN save goal intent WHEN goal saved THEN state updates`() = runTest {
        every { processHolder.processIntent(any()) } returns flowOf(GoalsResult.GoalSaved)

        val viewModel = GoalsViewModel(processHolder)

        viewModel.states().test {
            val state = awaitItem()
            state.displayLoading shouldBeEqualTo false
        }
    }
}
