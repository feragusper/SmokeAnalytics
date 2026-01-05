package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeCount
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class FetchSmokeStatsUseCaseTest {

    private val repository = FakeSmokeRepository()
    private val useCase = FetchSmokeStatsUseCase(repository)

    @Test
    fun `GIVEN a day period WHEN invoke is executed THEN it should return correct smoke statistics for the day`() =
        runTest {
            val smokeList = listOf(
                Smoke("1", Instant.parse("2023-03-01T12:00:00Z"), 0L to 0L),
                Smoke("2", Instant.parse("2023-03-01T14:00:00Z"), 0L to 0L)
            )
            repository.smokesToReturn = smokeList

            val result = useCase.invoke(2023, 3, 1, FetchSmokeStatsUseCase.PeriodType.DAY)

            val expectedStats = SmokeStats.from(smokeList, 2023, 3, 1)
            assertEquals(expectedStats, result)
        }

    @Test
    fun `GIVEN a week period WHEN invoke is executed THEN it should return correct smoke statistics for the week`() =
        runTest {
            val smokeList = listOf(
                Smoke("1", Instant.parse("2023-03-01T12:00:00Z"), 0L to 0L),
                Smoke("2", Instant.parse("2023-03-02T14:00:00Z"), 0L to 0L),
                Smoke("3", Instant.parse("2023-03-05T13:00:00Z"), 0L to 0L)
            )
            repository.smokesToReturn = smokeList

            val result = useCase.invoke(2023, 3, 1, FetchSmokeStatsUseCase.PeriodType.WEEK)

            val expectedStats = SmokeStats.from(smokeList, 2023, 3, 1)
            assertEquals(expectedStats, result)
        }

    @Test
    fun `GIVEN a month period WHEN invoke is executed THEN it should return correct smoke statistics for the month`() =
        runTest {
            val smokeList = listOf(
                Smoke("1", Instant.parse("2023-03-01T12:00:00Z"), 0L to 0L),
                Smoke("2", Instant.parse("2023-03-05T13:00:00Z"), 0L to 0L),
                Smoke("3", Instant.parse("2023-03-15T14:00:00Z"), 0L to 0L)
            )
            repository.smokesToReturn = smokeList

            val result = useCase.invoke(2023, 3, 1, FetchSmokeStatsUseCase.PeriodType.MONTH)

            val expectedStats = SmokeStats.from(smokeList, 2023, 3, 1)
            assertEquals(expectedStats, result)
        }

    @Test
    fun `GIVEN a year period WHEN invoke is executed THEN it should return correct smoke statistics for the year`() =
        runTest {
            val smokeList = listOf(
                Smoke("1", Instant.parse("2023-03-01T12:00:00Z"), 0L to 0L),
                Smoke("2", Instant.parse("2023-03-05T13:00:00Z"), 0L to 0L),
                Smoke("3", Instant.parse("2023-04-15T14:00:00Z"), 0L to 0L)
            )
            repository.smokesToReturn = smokeList

            val result = useCase.invoke(2023, 3, 1, FetchSmokeStatsUseCase.PeriodType.YEAR)

            val expectedStats = SmokeStats.from(smokeList, 2023, 3, 1)
            assertEquals(expectedStats, result)
        }

    private class FakeSmokeRepository : SmokeRepository {
        var smokesToReturn: List<Smoke> = emptyList()

        override suspend fun fetchSmokes(
            startDate: Instant?,
            endDate: Instant?
        ): List<Smoke> = smokesToReturn

        override suspend fun addSmoke(date: Instant) = Unit
        override suspend fun editSmoke(id: String, date: Instant) = Unit
        override suspend fun deleteSmoke(id: String) = Unit
        override suspend fun fetchSmokeCount(): SmokeCount {
            throw UnsupportedOperationException("Not needed for this test")
        }
    }
}