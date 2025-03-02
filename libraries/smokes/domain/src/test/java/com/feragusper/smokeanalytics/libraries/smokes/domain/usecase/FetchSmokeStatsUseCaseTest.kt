package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.Month

class FetchSmokeStatsUseCaseTest {

    private val repository: SmokeRepository = mockk() // Create a mock of the SmokeRepository
    private val useCase =
        FetchSmokeStatsUseCase(repository) // Instantiate the use case with the mock

    /**
     * Verifies that the use case correctly fetches smoke statistics for the specified day.
     */
    @Test
    fun `GIVEN a day period WHEN invoke is executed THEN it should return correct smoke statistics for the day`() =
        runTest {
            val smokeList = listOf(
                Smoke("1", LocalDateTime.of(2023, Month.MARCH, 1, 12, 0, 0, 0), Pair(0L, 0L)),
                Smoke("2", LocalDateTime.of(2023, Month.MARCH, 1, 14, 0, 0, 0), Pair(0L, 0L))
            )

            // Mock the repository to return a predefined list of smoke events
            coEvery { repository.fetchSmokes(any(), any()) } returns smokeList

            // Act: Call the use case to fetch smoke statistics for the day
            val result = useCase.invoke(2023, 3, 1, FetchSmokeStatsUseCase.PeriodType.DAY)

            // Create the expected result (SmokeStats for that day)
            val expectedStats = SmokeStats.from(smokeList, 2023, 3, 1)

            // Assert: Verify that the result matches the expected smoke statistics
            result shouldBeEqualTo expectedStats
        }

    /**
     * Verifies that the use case correctly fetches smoke statistics for the specified week.
     */
    @Test
    fun `GIVEN a week period WHEN invoke is executed THEN it should return correct smoke statistics for the week`() =
        runTest {
            val smokeList = listOf(
                Smoke("1", LocalDateTime.of(2023, Month.MARCH, 1, 12, 0, 0, 0), Pair(0L, 0L)),
                Smoke("2", LocalDateTime.of(2023, Month.MARCH, 2, 14, 0, 0, 0), Pair(0L, 0L)),
                Smoke("3", LocalDateTime.of(2023, Month.MARCH, 5, 13, 0, 0, 0), Pair(0L, 0L))
            )

            // Mock the repository to return a predefined list of smoke events
            coEvery { repository.fetchSmokes(any(), any()) } returns smokeList

            // Act: Call the use case to fetch smoke statistics for the week
            val result = useCase.invoke(2023, 3, 1, FetchSmokeStatsUseCase.PeriodType.WEEK)

            // Create the expected result (SmokeStats for that week)
            val expectedStats = SmokeStats.from(smokeList, 2023, 3, 1)

            // Assert: Verify that the result matches the expected smoke statistics
            result shouldBeEqualTo expectedStats
        }

    /**
     * Verifies that the use case correctly fetches smoke statistics for the specified month.
     */
    @Test
    fun `GIVEN a month period WHEN invoke is executed THEN it should return correct smoke statistics for the month`() =
        runTest {
            val smokeList = listOf(
                Smoke("1", LocalDateTime.of(2023, Month.MARCH, 1, 12, 0, 0, 0), Pair(0L, 0L)),
                Smoke("2", LocalDateTime.of(2023, Month.MARCH, 5, 13, 0, 0, 0), Pair(0L, 0L)),
                Smoke("3", LocalDateTime.of(2023, Month.MARCH, 15, 14, 0, 0, 0), Pair(0L, 0L))
            )

            // Mock the repository to return a predefined list of smoke events
            coEvery { repository.fetchSmokes(any(), any()) } returns smokeList

            // Act: Call the use case to fetch smoke statistics for the month
            val result = useCase.invoke(2023, 3, 1, FetchSmokeStatsUseCase.PeriodType.MONTH)

            // Create the expected result (SmokeStats for that month)
            val expectedStats = SmokeStats.from(smokeList, 2023, 3, 1)

            // Assert: Verify that the result matches the expected smoke statistics
            result shouldBeEqualTo expectedStats
        }

    /**
     * Verifies that the use case correctly fetches smoke statistics for the specified year.
     */
    @Test
    fun `GIVEN a year period WHEN invoke is executed THEN it should return correct smoke statistics for the year`() =
        runTest {
            val smokeList = listOf(
                Smoke("1", LocalDateTime.of(2023, Month.MARCH, 1, 12, 0, 0, 0), Pair(0L, 0L)),
                Smoke("2", LocalDateTime.of(2023, Month.MARCH, 5, 13, 0, 0, 0), Pair(0L, 0L)),
                Smoke("3", LocalDateTime.of(2023, Month.APRIL, 15, 14, 0, 0, 0), Pair(0L, 0L))
            )

            // Mock the repository to return a predefined list of smoke events
            coEvery { repository.fetchSmokes(any(), any()) } returns smokeList

            // Act: Call the use case to fetch smoke statistics for the year
            val result = useCase.invoke(2023, 3, 1, FetchSmokeStatsUseCase.PeriodType.YEAR)

            // Create the expected result (SmokeStats for that year)
            val expectedStats = SmokeStats.from(smokeList, 2023, 3, 1)

            // Adjust expected daily value, totalDay should now match the actual count of smokes in the year
            expectedStats.totalDay shouldBeEqualTo 1

            // Assert: Verify that the result matches the expected smoke statistics
            result shouldBeEqualTo expectedStats
        }


}