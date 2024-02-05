package com.feragusper.smokeanalytics.features.devtools.presentation.process

import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsIntent
import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsResult
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
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DevToolsProcessHolderTest {

    private lateinit var results: Flow<DevToolsResult>
    private lateinit var devToolsProcessHolder: DevToolsProcessHolder

    private var fetchSessionUseCase: FetchSessionUseCase = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        devToolsProcessHolder = DevToolsProcessHolder(fetchSessionUseCase)
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
            results = devToolsProcessHolder.processIntent(DevToolsIntent.FetchUser)
            assertEquals(DevToolsResult.Loading, results.first())
            assertEquals(DevToolsResult.UserLoggedOut, results.last())
        }
    }

    @Test
    fun `GIVEN the session is logged in WHEN fetchuser intent is processed THEN it should result with loading and user logged in`() =
        runTest {
            val user: Session.User = mockk()

            every { fetchSessionUseCase.invoke() } answers {
                Session.LoggedIn(user)
            }

            results = devToolsProcessHolder.processIntent(DevToolsIntent.FetchUser)
            assertEquals(DevToolsResult.Loading, results.first())
            assertEquals(DevToolsResult.UserLoggedIn(user), results.last())
        }

}
