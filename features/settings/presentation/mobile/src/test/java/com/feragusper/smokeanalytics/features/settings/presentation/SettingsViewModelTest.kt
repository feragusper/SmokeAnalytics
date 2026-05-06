package com.feragusper.smokeanalytics.features.settings.presentation

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsResult
import com.feragusper.smokeanalytics.features.settings.presentation.process.SettingsProcessHolder
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val processHolder: SettingsProcessHolder = mockk(relaxed = true)
    private lateinit var viewModel: SettingsViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        every { processHolder.processIntent(SettingsIntent.FetchUser) } returns flowOf(
            SettingsResult.Loading
        )
        viewModel = SettingsViewModel(processHolder)
    }

    @Test
    fun `GIVEN ViewModel is created THEN it should emit FetchUser intent and display loading`() =
        runTest {
            viewModel.states().test {
                awaitItem().displayLoading shouldBeEqualTo true
            }
        }

    @Test
    fun `GIVEN a loading result WHEN emitted THEN it displays loading`() =
        runTest {
            every { processHolder.processIntent(SettingsIntent.FetchUser) } returns flowOf(
                SettingsResult.Loading
            )

            viewModel.states().test {
                awaitItem().displayLoading shouldBeEqualTo true
            }
        }

    @Test
    fun `GIVEN a user logged out result WHEN emitted THEN it shows logged out state`() =
        runTest {
            every { processHolder.processIntent(SettingsIntent.FetchUser) } returns flowOf(
                SettingsResult.UserLoggedOut
            )

            viewModel.states().test {
                val state = awaitItem()
                state.displayLoading shouldBeEqualTo false
                state.currentEmail shouldBeEqualTo null
            }
        }

    @Test
    fun `GIVEN a user logged in result WHEN emitted THEN it updates UI state`() =
        runTest {
            val email = "fernando@example.com"
            val displayName = "Fernando Perez"
            every { processHolder.processIntent(SettingsIntent.FetchUser) } returns flowOf(
                SettingsResult.UserLoggedIn(email, displayName, UserPreferences())
            )

            viewModel.states().test {
                val state = awaitItem()
                state.displayLoading shouldBeEqualTo false
                state.currentEmail shouldBeEqualTo email
                state.currentDisplayName shouldBeEqualTo displayName
            }
        }

    @Test
    fun `GIVEN a user logs in THEN signs out THEN it updates state correctly`() =
        runTest {
            val email = "fernando@example.com"
            val displayName = "Fernando Perez"
            every { processHolder.processIntent(SettingsIntent.FetchUser) } returns flowOf(
                SettingsResult.UserLoggedIn(email, displayName, UserPreferences())
            )
            every { processHolder.processIntent(SettingsIntent.SignOut) } returns flowOf(
                SettingsResult.UserLoggedOut
            )

            viewModel.states().test {
                // Initial state after login
                var state = awaitItem()
                state.displayLoading shouldBeEqualTo false
                state.currentEmail shouldBeEqualTo email
                state.currentDisplayName shouldBeEqualTo displayName

                // Simulating Sign Out
                viewModel.intents().trySend(SettingsIntent.SignOut)

                state = awaitItem()
                state.displayLoading shouldBeEqualTo false
                state.currentEmail shouldBeEqualTo null
                state.currentDisplayName shouldBeEqualTo null
            }
        }
}
