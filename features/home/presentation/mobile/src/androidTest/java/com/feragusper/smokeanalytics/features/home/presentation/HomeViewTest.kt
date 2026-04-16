package com.feragusper.smokeanalytics.features.home.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.goals.domain.GoalStatus
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.compose.HomeViewState
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Instant
import org.junit.Rule
import org.junit.Test

class HomeViewTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val viewModel: HomeViewModel = mockk(relaxed = true)

    @Test
    fun testDisplayButton() {
        prepareScreen()

        composeTestRule.onNodeWithTag(HomeViewState.TestTags.BUTTON_ADD_SMOKE).assertIsDisplayed()
    }

    @Test
    fun testButtonClick() {
        prepareScreen()

        composeTestRule.onNodeWithTag(HomeViewState.TestTags.BUTTON_ADD_SMOKE).performClick()
        verify { viewModel.intents().trySend(HomeIntent.AddSmoke) }
    }

    @Test
    fun goalFirstHomeShowsGoalAndLastCigaretteContext() {
        prepareScreen(
            displayLoading = false,
            state = HomeViewState(
                displayLoading = false,
                smokesPerDay = 3,
                timeSinceLastCigarette = 2L to 35L,
                lastSmoke = Smoke(
                    id = "last",
                    date = Instant.parse("2026-04-15T14:10:00Z"),
                    timeElapsedSincePreviousSmoke = 1L to 10L,
                ),
                goalProgress = GoalProgress(
                    goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 6),
                    title = "Daily cap",
                    targetLabel = "Target: at most 6 today",
                    progressLabel = "3 / 6 smoked today",
                    supportingText = "3 left before reaching today's cap.",
                    status = GoalStatus.OnTrack,
                    progressFraction = 0.5f,
                ),
            ),
        )

        composeTestRule.onNodeWithText("3 cigarettes left today").assertIsDisplayed()
        composeTestRule.onNodeWithText("Last cigarette").assertIsDisplayed()
        composeTestRule.onNodeWithText("Time since").assertIsDisplayed()
        composeTestRule.onNodeWithText("2h 35m").assertIsDisplayed()
    }

    private fun prepareScreen(
        displayLoading: Boolean = true,
        state: HomeViewState = HomeViewState(
            displayLoading = displayLoading,
        ),
    ) {
        every { viewModel.states() } returns MutableStateFlow(state)
        composeTestRule.setContent {
            HomeView(viewModel) { _, _, _ -> }
        }
    }
}
