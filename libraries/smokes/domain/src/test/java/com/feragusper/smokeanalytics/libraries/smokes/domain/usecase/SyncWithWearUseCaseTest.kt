package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.wear.domain.WearSyncManager
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SyncWithWearUseCaseTest {

    private val wearSyncManager: WearSyncManager.Mobile = mockk()

    private lateinit var syncWithWearUseCase: SyncWithWearUseCase

    /**
     * Sets up the test by initializing the use case and configuring default mock behaviors.
     */
    @BeforeEach
    fun setUp() {
        syncWithWearUseCase = SyncWithWearUseCase(wearSyncManager)

        // Default mock behavior: Just execute without returning anything
        coEvery { wearSyncManager.syncWithWear() } just Runs
    }

    /**
     * Ensures that invoking the use case calls `syncWithWear()` on the WearSyncManager.
     */
    @Test
    fun `WHEN invoke is executed THEN it should call syncWithWear`() = runTest {
        // Act: invoke the use case
        syncWithWearUseCase.invoke()

        // Assert: verify that `syncWithWear()` was called once
        coVerify(exactly = 1) { wearSyncManager.syncWithWear() }
    }
}
