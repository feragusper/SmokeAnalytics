package com.feragusper.smokeanalytics.features.profile.presentation

import com.feragusper.smokeanalytics.features.profile.presentation.mvi.ProfileIntent
import com.feragusper.smokeanalytics.features.profile.presentation.mvi.ProfileResult
import com.feragusper.smokeanalytics.features.profile.presentation.mvi.ProfileViewState
import com.feragusper.smokeanalytics.features.profile.presentation.process.ProfileProcessHolder
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

class ProfileViewModelTest {

    private var processHolder: ProfileProcessHolder = mockk()
    private lateinit var state: ProfileViewState
    private val intentResults = MutableStateFlow<ProfileResult>(ProfileResult.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        every { processHolder.processIntent(ProfileIntent.FetchUser) } returns intentResults
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a loading result WHEN viewmodel is created THEN it displays loading`() {
        runBlocking {
            intentResults.emit(ProfileResult.Loading)
            state = ProfileViewModel(processHolder).states().first()
        }

        state.displayLoading shouldBeEqualTo true
    }

    @Test
    fun `GIVEN a user logged out result WHEN viewmodel is created THEN it shows logged out state`() {
        runBlocking {
            intentResults.emit(ProfileResult.UserLoggedOut)
            state = ProfileViewModel(processHolder).states().first()
        }

        state.displayLoading shouldBeEqualTo false
        state.currentUserName shouldBeEqualTo null
    }

    @Nested
    inner class UserLoggedIn {

        private lateinit var viewModel: ProfileViewModel

        @BeforeEach
        fun setUp() {
            runBlocking {
                intentResults.emit(ProfileResult.UserLoggedIn("Fernando Perez"))
                viewModel = ProfileViewModel(processHolder)
            }
        }

        @Test
        fun `GIVEN a user logged in result WHEN viewmodel is created THEN shows logged in state`() {
            runBlocking {
                state = viewModel.states().first()
            }

            state.displayLoading shouldBeEqualTo false
            state.currentUserName shouldBeEqualTo "Fernando Perez"
        }

        @Test
        fun `GIVEN a user logged in result WHEN sign out is sent THEN shows logged out state`() {
            every { processHolder.processIntent(ProfileIntent.SignOut) } returns intentResults
            viewModel.intents().trySend(ProfileIntent.SignOut)

            runBlocking {
                intentResults.emit(ProfileResult.UserLoggedOut)
                state = viewModel.states().first()
            }

            state.displayLoading shouldBeEqualTo false
            state.currentUserName shouldBeEqualTo null
        }
    }
}
