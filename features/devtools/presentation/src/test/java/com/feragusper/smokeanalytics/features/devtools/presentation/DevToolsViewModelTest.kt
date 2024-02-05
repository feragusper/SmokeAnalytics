package com.feragusper.smokeanalytics.features.devtools.presentation

import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsIntent
import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsResult
import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsViewState
import com.feragusper.smokeanalytics.features.devtools.presentation.process.DevToolsProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
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

class DevToolsViewModelTest {

    private var processHolder: DevToolsProcessHolder = mockk()
    private lateinit var state: DevToolsViewState
    private val intentResults = MutableStateFlow<DevToolsResult>(DevToolsResult.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        every { processHolder.processIntent(DevToolsIntent.FetchUser) } returns intentResults
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a loading result WHEN viewmodel is created THEN it displays loading`() {
        runBlocking {
            intentResults.emit(DevToolsResult.Loading)
            state = DevToolsViewModel(processHolder).states().first()
        }

        state.displayLoading shouldBeEqualTo true
    }

    @Test
    fun `GIVEN a user logged out result WHEN viewmodel is created THEN it shows logged out state`() {
        runBlocking {
            intentResults.emit(DevToolsResult.UserLoggedOut)
            state = DevToolsViewModel(processHolder).states().first()
        }

        state.displayLoading shouldBeEqualTo false
        state.currentUser shouldBeEqualTo null
    }

    @Nested
    inner class UserLoggedIn {

        private lateinit var viewModel: DevToolsViewModel
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
            runBlocking {
                intentResults.emit(DevToolsResult.UserLoggedIn(user))
                viewModel = DevToolsViewModel(processHolder)
            }
        }

        @Test
        fun `GIVEN a user logged in result WHEN viewmodel is created THEN shows logged in state`() {
            runBlocking {
                state = viewModel.states().first()
            }

            state.displayLoading shouldBeEqualTo false
            state.currentUser shouldBeEqualTo DevToolsViewState.User(
                id = id,
                email = email
            )
        }

    }
}
