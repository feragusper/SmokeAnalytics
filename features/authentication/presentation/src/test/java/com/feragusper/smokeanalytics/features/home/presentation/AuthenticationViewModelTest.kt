package com.feragusper.smokeanalytics.features.home.presentation


import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationViewModel
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationIntent
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationResult
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.compose.AuthenticationViewState
import com.feragusper.smokeanalytics.features.authentication.presentation.process.AuthenticationProcessHolder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthenticationViewModelTest {

    private var processHolder: AuthenticationProcessHolder = mockk()
    private lateinit var state: AuthenticationViewState
    private val intentResults = MutableStateFlow<AuthenticationResult>(AuthenticationResult.Loading)
    private lateinit var viewModel: AuthenticationViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        every { processHolder.processIntent(AuthenticationIntent.FetchUser) } returns intentResults
        viewModel = AuthenticationViewModel(processHolder)
        viewModel.navigator = mockk(relaxed = true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a user logged in result WHEN sign out is sent THEN shows logged out state`() =
        runTest {
            viewModel.intents().trySend(AuthenticationIntent.FetchUser)
            intentResults.emit(AuthenticationResult.UserLoggedIn)

            state = viewModel.states().first()

            verify { viewModel.navigator.navigateUp() }
        }

}
