package com.feragusper.smokeanalytics.features.home.presentation.process

import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationIntent
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationResult
import com.feragusper.smokeanalytics.features.authentication.presentation.process.AuthenticationProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
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
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthenticationProcessHolderTest {

    private lateinit var results: Flow<AuthenticationResult>
    private lateinit var processHolder: AuthenticationProcessHolder

    private var fetchSessionUseCase: FetchSessionUseCase = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        processHolder = AuthenticationProcessHolder(fetchSessionUseCase)
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
            results = processHolder.processIntent(AuthenticationIntent.FetchUser)
            results.first() shouldBe AuthenticationResult.Loading
            results.last() shouldBe AuthenticationResult.Error.Generic
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

            results = processHolder.processIntent(AuthenticationIntent.FetchUser)
            results.first() shouldBe AuthenticationResult.Loading
            results.last() shouldBe AuthenticationResult.UserLoggedIn
        }

}
