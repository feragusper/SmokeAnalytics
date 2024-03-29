package com.feragusper.smokeanalytics.features.settings.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.compose.SettingsViewState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class SettingsViewTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val viewModel: SettingsViewModel = mockk(relaxed = true)

    @Test
    fun testLoading() {
        prepareScreen()

        composeTestRule.onNodeWithTag(SettingsViewState.TestTags.BUTTON_SIGN_OUT)
            .assertDoesNotExist()
        composeTestRule.onNodeWithTag(SettingsViewState.TestTags.BUTTON_SIGN_IN).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SettingsViewState.TestTags.VIEW_PROGRESS).assertIsDisplayed()
    }

    @Test
    fun testSignedOut() {
        prepareScreen(
            displayLoading = false
        )

        composeTestRule.onNodeWithTag(SettingsViewState.TestTags.VIEW_PROGRESS).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SettingsViewState.TestTags.BUTTON_SIGN_OUT)
            .assertDoesNotExist()
        composeTestRule.onNodeWithTag(SettingsViewState.TestTags.BUTTON_SIGN_IN).assertIsDisplayed()
    }

    @Test
    fun testSignedIn() {
        prepareScreen(
            displayLoading = false,
            currentUserName = "Fernando Perez"
        )

        composeTestRule.onNodeWithTag(SettingsViewState.TestTags.VIEW_PROGRESS).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SettingsViewState.TestTags.BUTTON_SIGN_IN).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SettingsViewState.TestTags.BUTTON_SIGN_OUT).assertIsDisplayed()
    }

    @Test
    fun testButtonSignOutClick() {
        prepareScreen(
            displayLoading = false,
            currentUserName = "Fernando Perez"
        )

        composeTestRule.onNodeWithTag(SettingsViewState.TestTags.BUTTON_SIGN_OUT).performClick()
        verify { viewModel.intents().trySend(SettingsIntent.SignOut) }
    }

    private fun prepareScreen(
        displayLoading: Boolean = true,
        currentUserName: String? = null,
    ) {
        every { viewModel.states() } returns MutableStateFlow(
            SettingsViewState(
                displayLoading = displayLoading,
                currentEmail = currentUserName,
            )
        )
        composeTestRule.setContent {
            SettingsView(viewModel)
        }
    }
}