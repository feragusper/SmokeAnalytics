package com.feragusper.smokeanalytics.features.authentication.presentation.process

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationIntent
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationResult
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticationProcessHolderTest {

    private lateinit var processHolder: AuthenticationProcessHolder

    private val fetchSessionUseCase: FetchSessionUseCase = mockk()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        processHolder = AuthenticationProcessHolder(fetchSessionUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN the session is anonymous WHEN FetchUser intent is processed THEN emits Loading and Error_Generic`() =
        runTest {
            every { fetchSessionUseCase() } returns Session.Anonymous

            processHolder.processIntent(AuthenticationIntent.FetchUser).test {
                awaitItem() shouldBeEqualTo AuthenticationResult.Loading
                awaitItem() shouldBeEqualTo AuthenticationResult.Error.Generic
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN the session is logged in WHEN FetchUser intent is processed THEN emits Loading and UserLoggedIn`() =
        runTest {
            val user = Session.User(
                id = "123",
                email = "fernancho@gmail.com",
                displayName = "Fer"
            )

            every { fetchSessionUseCase.invoke() } returns Session.LoggedIn(user)

            processHolder.processIntent(AuthenticationIntent.FetchUser).test {
                awaitItem() shouldBeEqualTo AuthenticationResult.Loading
                awaitItem() shouldBeEqualTo AuthenticationResult.UserLoggedIn
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN navigate up intent WHEN processed THEN emits NavigateUp`() =
        runTest {
            processHolder.processIntent(AuthenticationIntent.NavigateUp).test {
                awaitItem() shouldBeEqualTo AuthenticationResult.NavigateUp
                awaitComplete()
            }
        }
}
