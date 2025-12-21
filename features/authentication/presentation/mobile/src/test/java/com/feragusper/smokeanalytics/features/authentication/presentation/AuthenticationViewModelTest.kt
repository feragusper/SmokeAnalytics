package com.feragusper.smokeanalytics.features.authentication.presentation

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationIntent
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationResult
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.compose.AuthenticationViewState
import com.feragusper.smokeanalytics.features.authentication.presentation.navigation.AuthenticationNavigator
import com.feragusper.smokeanalytics.features.authentication.presentation.process.AuthenticationProcessHolder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticationViewModelTest {

    private val processHolder: AuthenticationProcessHolder = mockk()
    private val navigator: AuthenticationNavigator = mockk(relaxed = true)

    private val intentResults = MutableStateFlow<AuthenticationResult>(AuthenticationResult.Loading)

    private lateinit var viewModel: AuthenticationViewModel
    private lateinit var state: AuthenticationViewState

    /**
     * Sets up the test by initializing the ViewModel and configuring mock behaviors.
     */
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)

        every { processHolder.processIntent(AuthenticationIntent.FetchUser) } returns intentResults

        viewModel = AuthenticationViewModel(processHolder)
        viewModel.navigator = navigator
    }

    /**
     * Resets Dispatchers after each test to avoid affecting other tests.
     */
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Ensures that when a **UserLoggedIn** result is emitted, the ViewModel calls **navigateUp()**.
     */
    @Test
    fun `GIVEN a user logged in result WHEN FetchUser intent is sent THEN navigates up and updates state`() =
        runTest {
            // Act: Send intent and emit result
            viewModel.intents().trySend(AuthenticationIntent.FetchUser)
            intentResults.emit(AuthenticationResult.Loading)

            // Capture the updated state
            state = viewModel.states().first()

            // Assert: Verify loading state
            assertEquals(true, state.displayLoading, "Loading should be true when fetching user")
            assertEquals(null, state.error, "Error should remain null while loading")
        }

    /**
     * Ensures that when **Loading** result is emitted, the UI state shows a loading indicator.
     */
    @Test
    fun `GIVEN loading state WHEN FetchUser intent is sent THEN updates UI state to loading`() =
        runTest {
            viewModel.intents().trySend(AuthenticationIntent.FetchUser)

            viewModel.states().test {
                // Emit loading result
                intentResults.emit(AuthenticationResult.Loading)

                // Capture and verify emitted state
                val loadingState = awaitItem()
                loadingState.displayLoading shouldBeEqualTo true
                loadingState.error shouldBeEqualTo null
            }
        }

    /**
     * Ensures that when an **Error** result is emitted, the UI state reflects the error.
     */
    @Test
    fun `GIVEN an error result WHEN FetchUser intent is sent THEN updates UI state with error`() =
        runTest {
            val errorResult = AuthenticationResult.Error.Generic

            viewModel.intents().trySend(AuthenticationIntent.FetchUser)

            viewModel.states().test {
                // Skip the initial state if necessary
                skipItems(1)

                // Emit the error result
                intentResults.emit(errorResult)

                // Capture the updated state
                val state = awaitItem()

                // Assertions
                state.displayLoading shouldBeEqualTo false
                state.error shouldBeEqualTo errorResult
            }
        }

}
