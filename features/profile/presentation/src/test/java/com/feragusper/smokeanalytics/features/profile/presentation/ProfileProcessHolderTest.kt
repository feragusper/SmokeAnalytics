package com.feragusper.smokeanalytics.features.profile.presentation

import com.feragusper.smokeanalytics.features.profile.presentation.mvi.ProfileIntent
import com.feragusper.smokeanalytics.features.profile.presentation.mvi.ProfileResult
import com.feragusper.smokeanalytics.features.profile.presentation.process.ProfileProcessHolder
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
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProfileProcessHolderTest {

    private lateinit var results: Flow<ProfileResult>
    private lateinit var profileProcessHolder: ProfileProcessHolder

    private var fetchSessionUseCase: FetchSessionUseCase = mockk()
    private var signOutUseCase: SignOutUseCase = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        profileProcessHolder = ProfileProcessHolder(fetchSessionUseCase, signOutUseCase)
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
            results = profileProcessHolder.processIntent(ProfileIntent.FetchUser)
            assertEquals(ProfileResult.Loading, results.first())
            assertEquals(ProfileResult.UserLoggedOut, results.last())
        }
    }

    @Test
    fun `GIVEN the session is logged in WHEN fetchuser intent is processed THEN it should result with loading and user logged in`() {
        every { fetchSessionUseCase.invoke() } answers { Session.LoggedIn(Session.User(displayName = "Fer")) }

        runBlocking {
            results = profileProcessHolder.processIntent(ProfileIntent.FetchUser)
            assertEquals(ProfileResult.Loading, results.first())
            assertEquals(ProfileResult.UserLoggedIn(displayName = "Fer"), results.last())
        }
    }

    @Test
    fun `WHEN the signout intent is processed THEN it should result with loading and user logged out`() {
        every { signOutUseCase.invoke() } answers { }

        runBlocking {
            results = profileProcessHolder.processIntent(ProfileIntent.SignOut)
            assertEquals(ProfileResult.Loading, results.first())
            assertEquals(ProfileResult.UserLoggedOut, results.last())
        }
    }
}
