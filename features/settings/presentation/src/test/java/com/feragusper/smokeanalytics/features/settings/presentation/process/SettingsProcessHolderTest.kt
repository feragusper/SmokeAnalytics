package com.feragusper.smokeanalytics.features.settings.presentation.process

import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsResult
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SettingsProcessHolderTest {

    private lateinit var results: Flow<SettingsResult>
    private lateinit var settingsProcessHolder: SettingsProcessHolder

    private var fetchSessionUseCase: FetchSessionUseCase = mockk()
    private var signOutUseCase: SignOutUseCase = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        settingsProcessHolder = SettingsProcessHolder(fetchSessionUseCase, signOutUseCase)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN the session is anonymous WHEN fetchuser intent is processed THEN it should result with loading and user logged out`() {
        every { fetchSessionUseCase() } answers { Session.Anonymous }

        runBlocking {
            results = settingsProcessHolder.processIntent(SettingsIntent.FetchUser)
            assertEquals(SettingsResult.Loading, results.first())
            assertEquals(SettingsResult.UserLoggedOut, results.last())
        }
    }

    @Test
    fun `GIVEN the session is logged in WHEN fetchuser intent is processed THEN it should result with loading and user logged in`() =
        runTest {
            val email = "fernancho@gmail.com"

            every { fetchSessionUseCase.invoke() } answers {
                Session.LoggedIn(
                    Session.User(
                        id = "123",
                        email = email,
                        displayName = "Fer"
                    )
                )
            }

            results = settingsProcessHolder.processIntent(SettingsIntent.FetchUser)
            assertEquals(SettingsResult.Loading, results.first())
            assertEquals(SettingsResult.UserLoggedIn(email = email), results.last())
        }

    @Test
    fun `WHEN the signout intent is processed THEN it should result with loading and user logged out`() {
        every { signOutUseCase.invoke() } answers { }

        runBlocking {
            results = settingsProcessHolder.processIntent(SettingsIntent.SignOut)
            assertEquals(SettingsResult.Loading, results.first())
            assertEquals(SettingsResult.UserLoggedOut, results.last())
        }
    }
}
