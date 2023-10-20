package com.feragusper.smokeanalytics.features.home.presentation.presentation

import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeViewState
import com.feragusper.smokeanalytics.features.home.presentation.presentation.process.HomeProcessHolder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HomeViewModelTest {

    private var processHolder: HomeProcessHolder = mockk()
    private val intentResults = MutableStateFlow<HomeResult>(HomeResult.Loading)
    private lateinit var viewModel: HomeViewModel
    private lateinit var state: HomeViewState

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        viewModel = HomeViewModel(processHolder)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN add smoke success result WHEN viewmodel is created THEN it hides loading`() {
        every { processHolder.processIntent(HomeIntent.AddSmoke) } returns intentResults

        runBlocking {
            intentResults.emit(HomeResult.AddSmokeSuccess)

            viewModel.intents().trySend(HomeIntent.AddSmoke)
            state = viewModel.states().first()
        }

        state.displayLoading shouldBeEqualTo false
    }

    @Test
    fun `GIVEN loading result WHEN viewmodel is created THEN it hides loading`() {
        every { processHolder.processIntent(HomeIntent.AddSmoke) } returns intentResults

        runBlocking {
            intentResults.emit(HomeResult.Loading)

            viewModel.intents().trySend(HomeIntent.AddSmoke)
            state = viewModel.states().first()
        }

        state.displayLoading shouldBeEqualTo true
    }
}
