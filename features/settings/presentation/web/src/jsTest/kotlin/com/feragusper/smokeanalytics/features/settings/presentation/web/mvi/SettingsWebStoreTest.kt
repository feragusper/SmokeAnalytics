package com.feragusper.smokeanalytics.features.settings.presentation.web.mvi

import com.feragusper.smokeanalytics.features.settings.presentation.web.process.SettingsProcessHolder
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsWebStoreTest {

    private val dispatcher = StandardTestDispatcher()
    private val processHolder: SettingsProcessHolder = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN preferences update succeeds WHEN goal is saved THEN store keeps the saved active goal`() = runTest(dispatcher) {
        val savedPreferences = UserPreferences(activeGoal = SmokingGoal.DailyCap(6))
        every { processHolder.processIntent(SettingsIntent.FetchUser) } returns flowOf(SettingsResult.UserLoggedOut)
        every {
            processHolder.processIntent(
                SettingsIntent.UpdatePreferences(savedPreferences)
            )
        } returns flowOf(
            SettingsResult.Loading,
            SettingsResult.UserLoggedIn(
                email = "fernando@example.com",
                displayName = "Fernando",
                preferences = savedPreferences,
                goalProgress = null,
            ),
            SettingsResult.PreferencesSaved,
        )

        val store = SettingsWebStore(processHolder = processHolder)
        store.start()
        advanceUntilIdle()

        store.send(SettingsIntent.UpdatePreferences(savedPreferences))
        advanceUntilIdle()

        store.state.value.preferences.activeGoal shouldBeEqualTo savedPreferences.activeGoal
        store.state.value.displayLoading shouldBeEqualTo false
    }
}
