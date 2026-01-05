package com.feragusper.smokeanalytics.features.stats.presentation

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.StatsIntent
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.StatsResult
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.compose.StatsViewState
import com.feragusper.smokeanalytics.features.stats.presentation.process.StatsProcessHolder
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val processHolder: StatsProcessHolder = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN loading state WHEN stats are requested THEN emits loading state first`() =
        runTest {
            every { processHolder.processIntent(any()) } returns flowOf(StatsResult.Loading)

            val viewModel = StatsViewModel(processHolder)

            viewModel.intents().trySend(
                StatsIntent.LoadStats(
                    year = 2025,
                    month = 3,
                    day = 2,
                    period = FetchSmokeStatsUseCase.PeriodType.WEEK
                )
            )

            viewModel.states().test {
                awaitItem() shouldBeEqualTo StatsViewState(
                    stats = null,
                )
            }
        }

    @Test
    fun `GIVEN a success result WHEN stats are received THEN it updates UI state correctly`() =
        runTest {
            val mockStats = SmokeStats(
                daily = mapOf("1" to 5, "2" to 6),
                weekly = mapOf("Mon" to 3, "Tue" to 4),
                monthly = mapOf("W1" to 10, "W2" to 20),
                yearly = mapOf("Jan" to 50, "Feb" to 60),
                hourly = mapOf("12:00" to 2, "14:00" to 3),
                totalMonth = 100,
                totalWeek = 30,
                totalDay = 5,
                dailyAverage = 3.5f
            )

            val selectedDate = LocalDate.of(2025, 3, 2)

            every { processHolder.processIntent(any()) } returns flowOf(
                StatsResult.Success(mockStats)
            )

            val viewModel = StatsViewModel(processHolder)

            viewModel.intents().trySend(
                StatsIntent.LoadStats(
                    year = 2025,
                    month = 3,
                    day = 2,
                    period = FetchSmokeStatsUseCase.PeriodType.WEEK
                )
            )

            viewModel.states().test {
                awaitItem() shouldBeEqualTo StatsViewState(
                    stats = mockStats,
                )
            }
        }

    @Test
    fun `GIVEN an error result WHEN stats fail to load THEN it resets stats`() =
        runTest {
            every { processHolder.processIntent(any()) } returns flowOf(
                StatsResult.Error(Exception("Stats loading failed"))
            )

            val viewModel = StatsViewModel(processHolder)

            viewModel.intents().trySend(
                StatsIntent.LoadStats(
                    year = 2025,
                    month = 3,
                    day = 2,
                    period = FetchSmokeStatsUseCase.PeriodType.WEEK
                )
            )

            viewModel.states().test {
                awaitItem() shouldBeEqualTo StatsViewState(
                    stats = null,
                )
            }
        }
}
