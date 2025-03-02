package com.feragusper.smokeanalytics.features.devtools.presentation.process

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsIntent
import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsResult
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DevToolsProcessHolderTest {

    private lateinit var devToolsProcessHolder: DevToolsProcessHolder
    private val fetchSessionUseCase: FetchSessionUseCase = mockk()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        devToolsProcessHolder = DevToolsProcessHolder(fetchSessionUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN the session is anonymous WHEN FetchUser intent is processed THEN result is Loading and UserLoggedOut`() =
        runTest {
            every { fetchSessionUseCase() } returns Session.Anonymous

            devToolsProcessHolder.processIntent(DevToolsIntent.FetchUser).test {
                awaitItem() shouldBeEqualTo DevToolsResult.Loading
                awaitItem() shouldBeEqualTo DevToolsResult.UserLoggedOut
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN the session is logged in WHEN FetchUser intent is processed THEN result is Loading and UserLoggedIn`() =
        runTest {
            val user =
                Session.User(id = "123", email = "test@example.com", displayName = "Test User")
            every { fetchSessionUseCase.invoke() } returns Session.LoggedIn(user)

            devToolsProcessHolder.processIntent(DevToolsIntent.FetchUser).test {
                awaitItem() shouldBeEqualTo DevToolsResult.Loading
                awaitItem() shouldBeEqualTo DevToolsResult.UserLoggedIn(user)
                awaitComplete()
            }
        }
}
