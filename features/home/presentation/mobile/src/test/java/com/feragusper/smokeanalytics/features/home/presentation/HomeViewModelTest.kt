package com.feragusper.smokeanalytics.features.home.presentation

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.features.home.presentation.process.HomeProcessHolder
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.Craving
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Clock
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

    @Test
    fun `WHEN a craving is tracked THEN it becomes the active craving`() = runTest {
        val craving = Craving(id = "c1", createdAt = Clock.System.now())
        every { processHolder.processIntent(HomeIntent.TrackCraving) } returns intentResults

        viewModel.states().test {
            viewModel.intents().trySend(HomeIntent.TrackCraving)
            intentResults.emit(HomeResult.CravingTracked(craving))

            var state = awaitItem()
            while (state.activeCraving == null) state = awaitItem()
            state.activeCraving shouldBeEqualTo craving
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN no wait is needed THEN the craving hint is shown`() = runTest {
        every { processHolder.processIntent(HomeIntent.TrackCraving) } returns intentResults

        viewModel.states().test {
            viewModel.intents().trySend(HomeIntent.TrackCraving)
            intentResults.emit(HomeResult.CravingNoWaitNeeded)

            var state = awaitItem()
            while (!state.showCravingHint) state = awaitItem()
            state.showCravingHint shouldBeEqualTo true
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN a craving is resolved with points THEN it celebrates and clears the active craving`() = runTest {
        every { processHolder.processIntent(HomeIntent.TrackCraving) } returns intentResults
        // The resolved reducer re-fetches; isolate that so it can't re-emit the result.
        every { processHolder.processIntent(HomeIntent.FetchSmokes) } returns
            MutableStateFlow(HomeResult.Loading)

        viewModel.states().test {
            viewModel.intents().trySend(HomeIntent.TrackCraving)
            intentResults.emit(HomeResult.CravingResolved(CravingOutcome.RESISTED, 18))

            var state = awaitItem()
            while (state.cravingCelebration == null) state = awaitItem()
            state.activeCraving shouldBeEqualTo null
            state.cravingCelebration?.points shouldBeEqualTo 18
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN the craving hint is dismissed THEN it is hidden`() = runTest {
        every { processHolder.processIntent(HomeIntent.DismissCravingHint) } returns intentResults

        viewModel.states().test {
            viewModel.intents().trySend(HomeIntent.DismissCravingHint)
            intentResults.emit(HomeResult.CravingNoWaitNeeded)
            var state = awaitItem()
            while (!state.showCravingHint) state = awaitItem()

            intentResults.emit(HomeResult.CravingHintDismissed)
            while (state.showCravingHint) state = awaitItem()
            state.showCravingHint shouldBeEqualTo false
            cancelAndIgnoreRemainingEvents()
        }
    }

}