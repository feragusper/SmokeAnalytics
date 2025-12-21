package com.feragusper.smokeanalytics.features.stats.presentation.process

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.StatsIntent
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.StatsResult
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsProcessHolderTest {

    private lateinit var processHolder: StatsProcessHolder
    private val fetchSmokeStatsUseCase: FetchSmokeStatsUseCase = mockk()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        processHolder = StatsProcessHolder(fetchSmokeStatsUseCase)
    }

    @Test
    fun `GIVEN valid inputs WHEN fetching stats THEN emits loading and success states`() = runTest {
        val year = 2025
        val month = 3
        val day = 2
        val period = FetchSmokeStatsUseCase.PeriodType.WEEK

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

        coEvery { fetchSmokeStatsUseCase(year, month, day, period) } returns mockStats

        processHolder.processIntent(StatsIntent.LoadStats(year, month, day, period)).test {
            awaitItem() shouldBeEqualTo StatsResult.Loading
            awaitItem() shouldBeEqualTo StatsResult.Success(mockStats)
            awaitComplete()
        }

        coVerify { fetchSmokeStatsUseCase(year, month, day, period) }
    }

    @Test
    fun `GIVEN an error occurs WHEN fetching stats THEN emits loading and error states`() =
        runTest {
            val year = 2025
            val month = 3
            val day = 2
            val period = FetchSmokeStatsUseCase.PeriodType.WEEK
            val exception = RuntimeException("Error fetching stats")

            coEvery { fetchSmokeStatsUseCase(year, month, day, period) } throws exception

            processHolder.processIntent(StatsIntent.LoadStats(year, month, day, period)).test {
                awaitItem() shouldBeEqualTo StatsResult.Loading
                awaitItem() shouldBeEqualTo StatsResult.Error(exception)
                awaitComplete()
            }

            coVerify { fetchSmokeStatsUseCase(year, month, day, period) }
        }
}
