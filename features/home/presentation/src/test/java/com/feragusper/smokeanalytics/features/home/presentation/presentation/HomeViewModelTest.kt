package com.feragusper.smokeanalytics.features.home.presentation.presentation

import com.feragusper.smokeanalytics.features.home.domain.Smoke
import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult
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
    private lateinit var state: HomeViewState

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        every { processHolder.processIntent(HomeIntent.FetchSmokes) } returns intentResults
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN fetch smokes result WHEN viewmodel is created THEN it shows smoke counts`() {
        val viewModel = HomeViewModel(processHolder)

        val smokesPerDay = 1
        val smokesPerWeek = 2
        val smokesPerMonth = 3
        val latestSmokes: List<Smoke> = listOf(mockk())
        runBlocking {
            intentResults.emit(
                HomeResult.FetchSmokesSuccess(
                    SmokeCountListResult(
                        byToday = smokesPerDay,
                        byWeek = smokesPerWeek,
                        byMonth = smokesPerMonth,
                        latestSmokes = latestSmokes
                    )
                )
            )
            state = viewModel.states().first()
        }

        state.displayLoading shouldBeEqualTo false
        state.smokeAddError shouldBeEqualTo null
        state.smokesPerDay shouldBeEqualTo smokesPerDay
        state.smokesPerWeek shouldBeEqualTo smokesPerWeek
        state.smokesPerMonth shouldBeEqualTo smokesPerMonth
        state.latestSmokes shouldBeEqualTo latestSmokes
    }

    @Test
    fun `GIVEN fetch smokes error WHEN viewmodel is created THEN it shows fetch smokes error`() {
        every { processHolder.processIntent(HomeIntent.AddSmoke) } returns intentResults

        val viewModel = HomeViewModel(processHolder)

        runBlocking {
            intentResults.emit(HomeResult.FetchSmokesError)

            viewModel.intents().trySend(HomeIntent.AddSmoke)
            state = viewModel.states().first()
        }

        state.displayLoading shouldBeEqualTo false
        state.displaySmokeAddedSuccess shouldBeEqualTo false
    }

    @Test
    fun `GIVEN loading result WHEN viewmodel is created THEN it hides loading`() {
        every { processHolder.processIntent(HomeIntent.AddSmoke) } returns intentResults

        val viewModel = HomeViewModel(processHolder)

        runBlocking {
            intentResults.emit(HomeResult.Loading)

            viewModel.intents().trySend(HomeIntent.AddSmoke)
            state = viewModel.states().first()
        }

        state.displayLoading shouldBeEqualTo true
    }

    @Test
    fun `GIVEN add smoke success result WHEN viewmodel is created THEN it hides loading and shows success`() {
        every { processHolder.processIntent(HomeIntent.AddSmoke) } returns intentResults

        val viewModel = HomeViewModel(processHolder)

        runBlocking {
            intentResults.emit(HomeResult.AddSmokeSuccess)

            viewModel.intents().trySend(HomeIntent.AddSmoke)
            state = viewModel.states().first()
        }

        state.displayLoading shouldBeEqualTo false
        state.displaySmokeAddedSuccess shouldBeEqualTo true
        state.smokeAddError shouldBeEqualTo null
    }

    @Test
    fun `GIVEN add smoke error result WHEN viewmodel is created THEN it hides loading and shows error`() {
        every { processHolder.processIntent(HomeIntent.AddSmoke) } returns intentResults

        val viewModel = HomeViewModel(processHolder)

        runBlocking {
            intentResults.emit(HomeResult.AddSmokeError.Generic)

            viewModel.intents().trySend(HomeIntent.AddSmoke)
            state = viewModel.states().first()
        }

        state.displayLoading shouldBeEqualTo false
        state.displaySmokeAddedSuccess shouldBeEqualTo false
        state.smokeAddError shouldBeEqualTo HomeResult.AddSmokeError.Generic
    }


}
