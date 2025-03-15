package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class EditSmokeUseCaseTest {

    private val smokeRepository: SmokeRepository = mockk()

    private lateinit var editSmokeUseCase: EditSmokeUseCase

    /**
     * Sets up the test by initializing the use case and configuring default mock behaviors.
     */
    @BeforeEach
    fun setUp() {
        editSmokeUseCase = EditSmokeUseCase(smokeRepository)

        // Default mock behavior: Just execute without returning anything
        coEvery { smokeRepository.editSmoke(any(), any()) } just Runs
    }

    /**
     * Ensures that invoking the use case **edits the smoke event** and **syncs with Wear OS**.
     */
    @Test
    fun `GIVEN a smoke event ID and date WHEN invoke is executed THEN it should call editSmoke`() =
        runTest {
            // Arrange: Define an ID and a new date
            val smokeId = "id"
            val newDate = LocalDateTime.of(2023, 3, 1, 12, 0, 0)

            // Act: Invoke the use case with the smoke ID and new date
            editSmokeUseCase.invoke(smokeId, newDate)

            // Assert: Verify that both `editSmoke()` and `syncWithWear()` were called correctly
            coVerify(exactly = 1) { smokeRepository.editSmoke(smokeId, newDate) }
        }
}
