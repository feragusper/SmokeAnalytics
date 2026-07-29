package com.feragusper.smokeanalytics.libraries.architecture.presentation

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AppStartupReadinessTest {

    @AfterEach
    fun tearDown() {
        // Restore the shared signal so tests stay isolated from each other.
        AppStartupReadiness.reset()
    }

    @Test
    fun `is not ready after a reset`() {
        AppStartupReadiness.reset()

        assertFalse(AppStartupReadiness.isReady.value)
    }

    @Test
    fun `markReady flips the signal to true`() {
        AppStartupReadiness.reset()

        AppStartupReadiness.markReady()

        assertTrue(AppStartupReadiness.isReady.value)
    }

    @Test
    fun `reset re-arms the signal after it was marked ready`() {
        AppStartupReadiness.markReady()

        AppStartupReadiness.reset()

        assertFalse(AppStartupReadiness.isReady.value)
    }
}
