package com.feragusper.smokeanalytics.features.home.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.feragusper.smokeanalytics.features.home.presentation.presentation.HomeView
import com.feragusper.smokeanalytics.features.home.presentation.presentation.HomeViewModel
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeViewState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

//@RunWith(AndroidJUnit4::class)
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

    private fun prepareScreen(
        displayLoading: Boolean = true,
        displaySmokeAddedSuccess: Boolean = false,
    ) {
        every { viewModel.states() } returns MutableStateFlow(
            HomeViewState(
                displayLoading = displayLoading,
                displaySmokeAddedSuccess = displaySmokeAddedSuccess,
            )
        )
        composeTestRule.setContent {
            HomeView(viewModel)
        }
    }
}