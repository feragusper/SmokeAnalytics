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
import java.time.LocalDateTime

class AddSmokeUseCaseTest {

    private val smokeRepository: SmokeRepository = mockk()
    private val wearSyncManager: WearSyncManager.Mobile = mockk()

    private lateinit var addSmokeUseCase: AddSmokeUseCase

    /**
     * Sets up the test by initializing the use case and configuring default mock behaviors.
     */
    @BeforeEach
    fun setUp() {
        addSmokeUseCase = AddSmokeUseCase(smokeRepository, wearSyncManager)

        // Default mock behavior: Just execute without returning anything
        coEvery { smokeRepository.addSmoke(any()) } just Runs
        coEvery { wearSyncManager.syncWithWear() } just Runs
    }

    /**
     * Ensures that invoking the use case **without a specific date** calls `addSmoke()` on the repository
     * and syncs with the Wear OS device.
     */
    @Test
    fun `GIVEN a smoke event WHEN invoke is executed THEN it should call addSmoke and syncWithWear`() =
        runTest {
            // Act: invoke the use case with the default date
            addSmokeUseCase.invoke()

            // Assert: verify that both `addSmoke()` and `syncWithWear()` were called
            coVerify(exactly = 1) { smokeRepository.addSmoke(any()) }
            coVerify(exactly = 1) { wearSyncManager.syncWithWear() }
        }

    /**
     * Ensures that invoking the use case with a **specific date** calls `addSmoke()` with that date
     * and syncs with the Wear OS device.
     */
    @Test
    fun `GIVEN a specific date WHEN invoke is executed THEN it should call addSmoke with that date and syncWithWear`() =
        runTest {
            // Arrange: define a specific date
            val specificDate = LocalDateTime.of(2023, 3, 1, 12, 0, 0)

            // Act: invoke the use case with the specific date
            addSmokeUseCase.invoke(specificDate)

            // Assert: verify that `addSmoke()` was called with the correct date
            coVerify(exactly = 1) { smokeRepository.addSmoke(specificDate) }
            coVerify(exactly = 1) { wearSyncManager.syncWithWear() }
        }
}
