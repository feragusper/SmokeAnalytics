package com.feragusper.smokeanalytics.features.settings.presentation.process

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsResult
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsProcessHolderTest {

    private lateinit var processHolder: SettingsProcessHolder

    private val fetchSessionUseCase: FetchSessionUseCase = mockk()
    private val signOutUseCase: SignOutUseCase = mockk()

    /**
     * Sets up the test environment by initializing the process holder and configuring mock behaviors.
     */
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        processHolder = SettingsProcessHolder(fetchSessionUseCase, signOutUseCase)
    }

    /**
     * Ensures that when the session is anonymous, fetching the user results in a logged-out state.
     */
    @Test
    fun `GIVEN session is anonymous WHEN FetchUser intent is processed THEN emit loading and UserLoggedOut`() =
        runTest {
            coEvery { fetchSessionUseCase() } returns Session.Anonymous

            processHolder.processIntent(SettingsIntent.FetchUser).test {
                awaitItem() shouldBeEqualTo SettingsResult.Loading
                awaitItem() shouldBeEqualTo SettingsResult.UserLoggedOut
                awaitComplete()
            }
        }

    /**
     * Ensures that when the session is logged in, fetching the user results in a logged-in state.
     */
    @Test
    fun `GIVEN session is logged in WHEN FetchUser intent is processed THEN emit loading and UserLoggedIn`() =
        runTest {
            val email = "fernancho@gmail.com"
            coEvery { fetchSessionUseCase() } returns Session.LoggedIn(
                Session.User(id = "123", email = email, displayName = "Fer")
            )

            processHolder.processIntent(SettingsIntent.FetchUser).test {
                awaitItem() shouldBeEqualTo SettingsResult.Loading
                awaitItem() shouldBeEqualTo SettingsResult.UserLoggedIn(email)
                awaitComplete()
            }
        }

    /**
     * Ensures that when the sign-out intent is processed, it results in a logged-out state.
     */
    @Test
    fun `WHEN SignOut intent is processed THEN emit loading and UserLoggedOut`() =
        runTest {
            coEvery { signOutUseCase() } just Runs

            processHolder.processIntent(SettingsIntent.SignOut).test {
                awaitItem() shouldBeEqualTo SettingsResult.Loading
                awaitItem() shouldBeEqualTo SettingsResult.UserLoggedOut
                awaitComplete()
            }

            // Verify that signOutUseCase() was actually called
            coVerify(exactly = 1) { signOutUseCase() }
        }
}
