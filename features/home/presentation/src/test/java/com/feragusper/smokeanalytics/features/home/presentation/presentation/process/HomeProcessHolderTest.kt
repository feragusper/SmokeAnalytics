package com.feragusper.smokeanalytics.features.home.presentation.presentation.process

import com.feragusper.smokeanalytics.features.home.domain.AddSmokeUseCase
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeResult
import io.mockk.coEvery
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

class HomeProcessHolderTest {

    private lateinit var results: Flow<HomeResult>
    private lateinit var processHolder: HomeProcessHolder

    private var addSmokeUseCase: AddSmokeUseCase = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        processHolder = HomeProcessHolder(addSmokeUseCase)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN add smoke is success WHEN add smoke intent is processed THEN it should result with loading and success`() {
        coEvery { addSmokeUseCase() } answers {}

        runBlocking {
            results = processHolder.processIntent(HomeIntent.AddSmoke)
            assertEquals(HomeResult.Loading, results.first())
            assertEquals(HomeResult.AddSmokeSuccess, results.last())
        }
    }


    @Test
    fun `GIVEN add smoke throws exception WHEN add smoke intent is processed THEN it should result with loading and error`() {
        coEvery { addSmokeUseCase() } throws (IllegalStateException("User not logged in"))

        runBlocking {
            results = processHolder.processIntent(HomeIntent.AddSmoke)
            assertEquals(HomeResult.Loading, results.first())
            assertEquals(HomeResult.AddSmokeError, results.last())
        }
    }

}
