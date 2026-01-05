package com.feragusper.smokeanalytics.features.home.presentation

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.features.home.presentation.process.HomeProcessHolder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: HomeViewModel
    private val processHolder: HomeProcessHolder = mockk()
    private val intentResults = MutableStateFlow<HomeResult>(HomeResult.Loading)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { processHolder.processIntent(HomeIntent.FetchSmokes) } returns intentResults
        viewModel = HomeViewModel(processHolder)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN edit smoke success and fetch smokes success WHEN edit smoke is sent THEN it updates state correctly`() =
        runTest {
            val id = "123"
            val date: Instant = Clock.System.now()

            every {
                processHolder.processIntent(
                    HomeIntent.EditSmoke(
                        id,
                        date
                    )
                )
            } returns intentResults

            viewModel.intents().trySend(HomeIntent.EditSmoke(id, date))
            intentResults.emit(HomeResult.EditSmokeSuccess)

            viewModel.states().test {
                awaitItem().displayLoading shouldBeEqualTo false
            }
        }

    @Test
    fun `GIVEN delete smoke success WHEN delete smoke is sent THEN it updates state correctly`() =
        runTest {
            val id = "123"
            every { processHolder.processIntent(HomeIntent.DeleteSmoke(id)) } returns intentResults

            viewModel.intents().trySend(HomeIntent.DeleteSmoke(id))
            intentResults.emit(HomeResult.DeleteSmokeSuccess)

            viewModel.states().test {
                awaitItem().displayLoading shouldBeEqualTo false
            }
        }

    @Test
    fun `GIVEN add smoke success WHEN add smoke is sent THEN it updates state correctly`() =
        runTest {
            every { processHolder.processIntent(HomeIntent.AddSmoke) } returns intentResults

            viewModel.intents().trySend(HomeIntent.AddSmoke)
            intentResults.emit(HomeResult.AddSmokeSuccess)

            viewModel.states().test {
                awaitItem().displayLoading shouldBeEqualTo false
            }
        }

}