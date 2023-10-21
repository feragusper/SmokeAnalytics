package com.feragusper.smokeanalytics.features.profile.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.feragusper.smokeanalytics.features.profile.presentation.mvi.ProfileIntent
import com.feragusper.smokeanalytics.features.profile.presentation.mvi.ProfileViewState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

//@RunWith(AndroidJUnit4::class)
class ProfileViewTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val viewModel: ProfileViewModel = mockk(relaxed = true)

    @Test
    fun testLoading() {
        prepareScreen()

        composeTestRule.onNodeWithTag(ProfileViewState.TestTags.BUTTON_SIGN_OUT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(ProfileViewState.TestTags.BUTTON_SIGN_IN).assertDoesNotExist()
        composeTestRule.onNodeWithTag(ProfileViewState.TestTags.VIEW_PROGRESS).assertIsDisplayed()
    }

    @Test
    fun testSignedOut() {
        prepareScreen(
            displayLoading = false
        )

        composeTestRule.onNodeWithTag(ProfileViewState.TestTags.VIEW_PROGRESS).assertDoesNotExist()
        composeTestRule.onNodeWithTag(ProfileViewState.TestTags.BUTTON_SIGN_OUT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(ProfileViewState.TestTags.BUTTON_SIGN_IN).assertIsDisplayed()
    }

    @Test
    fun testSignedIn() {
        prepareScreen(
            displayLoading = false,
            currentUserName = "Fernando Perez"
        )

        composeTestRule.onNodeWithTag(ProfileViewState.TestTags.VIEW_PROGRESS).assertDoesNotExist()
        composeTestRule.onNodeWithTag(ProfileViewState.TestTags.BUTTON_SIGN_IN).assertDoesNotExist()
        composeTestRule.onNodeWithTag(ProfileViewState.TestTags.BUTTON_SIGN_OUT).assertIsDisplayed()
    }

    @Test
    fun testButtonSignOutClick() {
        prepareScreen(
            displayLoading = false,
            currentUserName = "Fernando Perez"
        )

        composeTestRule.onNodeWithTag(ProfileViewState.TestTags.BUTTON_SIGN_OUT).performClick()
        verify { viewModel.intents().trySend(ProfileIntent.SignOut) }
    }

    private fun prepareScreen(
        displayLoading: Boolean = true,
        currentUserName: String? = null,
    ) {
        every { viewModel.states() } returns MutableStateFlow(
            ProfileViewState(
                displayLoading = displayLoading,
                currentUserName = currentUserName,
            )
        )
        composeTestRule.setContent {
            ProfileView(viewModel)
        }
    }
}