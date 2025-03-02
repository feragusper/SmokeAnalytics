package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.feragusper.smokeanalytics.libraries.wear.domain.WearSyncManager
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteSmokeUseCaseTest {

    private val smokeRepository: SmokeRepository = mockk()
    private val wearSyncManager: WearSyncManager.Mobile = mockk()

    private lateinit var deleteSmokeUseCase: DeleteSmokeUseCase

    /**
     * Sets up the test by initializing the use case and configuring default mock behaviors.
     */
    @BeforeEach
    fun setUp() {
        deleteSmokeUseCase = DeleteSmokeUseCase(smokeRepository, wearSyncManager)

        // Default mock behavior: Just execute without returning anything
        coEvery { smokeRepository.deleteSmoke(any()) } just Runs
        coEvery { wearSyncManager.syncWithWear() } just Runs
    }

    /**
     * Ensures that invoking the use case **deletes the smoke event** and **syncs with Wear OS**.
     */
    @Test
    fun `GIVEN a smoke event id WHEN invoke is executed THEN it should call deleteSmoke and syncWithWear`() =
        runTest {
            // Arrange: Define a smoke event ID
            val smokeId = "id"

            // Act: Invoke the use case with the smoke ID
            deleteSmokeUseCase.invoke(smokeId)

            // Assert: Verify that both `deleteSmoke()` and `syncWithWear()` were called
            coVerify(exactly = 1) { smokeRepository.deleteSmoke(smokeId) }
            coVerify(exactly = 1) { wearSyncManager.syncWithWear() }
        }
}
