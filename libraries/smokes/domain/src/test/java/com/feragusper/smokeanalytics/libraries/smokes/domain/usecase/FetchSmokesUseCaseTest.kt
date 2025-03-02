package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class FetchSmokesUseCaseTest {

    private val repository: SmokeRepository = mockk() // Create a mock of the SmokeRepository
    private val useCase = FetchSmokesUseCase(repository) // Instantiate the use case with the mock

    /**
     * Verifies that the use case correctly fetches smoke events from the repository.
     */
    @Test
    fun `GIVEN fetch smokes by date answers WHEN invoke with date is executed THEN it should return the correct data`() =
        runTest {
            val startDate = LocalDateTime.of(2023, 3, 1, 0, 0, 0, 0)
            val endDate = LocalDateTime.of(2023, 3, 31, 23, 59, 59, 999999)
            val smokeList = listOf(
                Smoke("1", LocalDateTime.of(2023, 3, 1, 12, 0, 0, 0), Pair(0L, 0L)),
                Smoke("2", LocalDateTime.of(2023, 3, 5, 13, 0, 0, 0), Pair(0L, 0L))
            )

            // Mock the repository to return a predefined list of smoke events
            coEvery { repository.fetchSmokes(startDate, endDate) } returns smokeList

            // Act: Call the use case to fetch the smoke events
            val result = useCase.invoke(startDate, endDate)

            // Assert: Verify that the result matches the expected list of smoke events
            assertEquals(smokeList, result)
        }
}
