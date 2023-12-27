package com.feragusper.smokeanalytics.features.settings.presentation

import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsResult
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsViewState
import com.feragusper.smokeanalytics.features.settings.presentation.process.SettingsProcessHolder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SettingsViewModelTest {

    private var processHolder: SettingsProcessHolder = mockk()
    private lateinit var state: SettingsViewState
    private val intentResults = MutableStateFlow<SettingsResult>(SettingsResult.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        every { processHolder.processIntent(SettingsIntent.FetchUser) } returns intentResults
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a loading result WHEN viewmodel is created THEN it displays loading`() {
        runBlocking {
            intentResults.emit(SettingsResult.Loading)
            state = SettingsViewModel(processHolder).states().first()
        }

        state.displayLoading shouldBeEqualTo true
    }

    @Test
    fun `GIVEN a user logged out result WHEN viewmodel is created THEN it shows logged out state`() {
        runBlocking {
            intentResults.emit(SettingsResult.UserLoggedOut)
            state = SettingsViewModel(processHolder).states().first()
        }

        state.displayLoading shouldBeEqualTo false
        state.currentEmail shouldBeEqualTo null
    }

    @Nested
    inner class UserLoggedIn {

        private lateinit var viewModel: SettingsViewModel

        @BeforeEach
        fun setUp() {
            runBlocking {
                intentResults.emit(SettingsResult.UserLoggedIn("Fernando Perez"))
                viewModel = SettingsViewModel(processHolder)
            }
        }

        @Test
        fun `GIVEN a user logged in result WHEN viewmodel is created THEN shows logged in state`() {
            runBlocking {
                state = viewModel.states().first()
            }

            state.displayLoading shouldBeEqualTo false
            state.currentEmail shouldBeEqualTo "Fernando Perez"
        }

        @Test
        fun `GIVEN a user logged in result WHEN sign out is sent THEN shows logged out state`() {
            every { processHolder.processIntent(SettingsIntent.SignOut) } returns intentResults
            viewModel.intents().trySend(SettingsIntent.SignOut)

            runBlocking {
                intentResults.emit(SettingsResult.UserLoggedOut)
                state = viewModel.states().first()
            }

            state.displayLoading shouldBeEqualTo false
            state.currentEmail shouldBeEqualTo null
        }
    }
}
