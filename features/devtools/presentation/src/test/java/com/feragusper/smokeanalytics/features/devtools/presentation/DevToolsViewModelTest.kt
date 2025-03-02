package com.feragusper.smokeanalytics.features.devtools.presentation

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsResult
import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.compose.DevToolsViewState
import com.feragusper.smokeanalytics.features.devtools.presentation.process.DevToolsProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DevToolsViewModelTest {

    private val processHolder: DevToolsProcessHolder = mockk(relaxed = true)
    private val intentResults = MutableStateFlow<DevToolsResult>(DevToolsResult.Loading)
    private lateinit var viewModel: DevToolsViewModel

    /**
     * Sets up the test by initializing the ViewModel and configuring mock behaviors.
     */
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)

        every { processHolder.processIntent(any()) } returns intentResults

        viewModel = DevToolsViewModel(processHolder)

        // Ensure coroutine execution before verification
        runTest { advanceUntilIdle() }
    }

    /**
     * Resets Dispatchers after each test to avoid affecting other tests.
     */
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Ensures that when a **loading result is emitted**, the UI state updates correctly.
     */
    @Test
    fun `GIVEN a loading result WHEN emitted THEN it displays loading`() = runTest {
        viewModel.states().test {
            intentResults.emit(DevToolsResult.Loading)

            awaitItem().displayLoading shouldBeEqualTo true

            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Ensures that when a **UserLoggedOut result is emitted**, the UI state updates correctly.
     */
    @Test
    fun `GIVEN a user logged out result WHEN emitted THEN it shows logged out state`() = runTest {
        viewModel.states().test {
            // First emission: Simulate loading state (if needed)
            intentResults.emit(DevToolsResult.Loading)
            awaitItem().displayLoading shouldBeEqualTo true

            // Emit user logged out result
            intentResults.emit(DevToolsResult.UserLoggedOut)

            // Await new state and verify expectations
            val state = awaitItem()

            state.displayLoading shouldBeEqualTo false  // Ensure loading is cleared
            state.currentUser shouldBeEqualTo null      // Ensure user is logged out

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Nested
    inner class UserLoggedIn {

        private val id = "1"
        private val email = "fernancho@gmail.com"
        private val displayName = "Fernando Perez"

        private val user: Session.User = mockk<Session.User>().apply {
            every { this@apply.id } returns this@UserLoggedIn.id
            every { this@apply.email } returns this@UserLoggedIn.email
            every { this@apply.displayName } returns this@UserLoggedIn.displayName
        }

        @BeforeEach
        fun setUp() {
            intentResults.value = DevToolsResult.UserLoggedIn(user)
        }

        /**
         * Ensures that when a **UserLoggedIn result is emitted**, the UI state updates correctly.
         */
        @Test
        fun `GIVEN a user logged in result WHEN emitted THEN shows logged in state`() = runTest {
            val sessionUser = Session.User(id = id, email = email, displayName = "Test User")

            viewModel.states().test {
                // Emit UserLoggedIn with Session.User
                intentResults.emit(DevToolsResult.UserLoggedIn(sessionUser))

                // Await updated state
                val state = awaitItem()

                // Extract expected DevToolsViewState.User from Session.User
                val expectedUser =
                    DevToolsViewState.User(id = sessionUser.id, email = sessionUser.email)

                state.displayLoading shouldBeEqualTo false
                state.currentUser shouldBeEqualTo expectedUser

                cancelAndIgnoreRemainingEvents() // Clean up
            }
        }

    }
}
