package com.feragusper.smokeanalytics.features.home.presentation.presentation.process

import com.feragusper.smokeanalytics.features.home.domain.AddSmokeUseCase
import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult
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
    private var fetchSmokeCountListUseCase: FetchSmokeCountListUseCase = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        processHolder = HomeProcessHolder(
            addSmokeUseCase = addSmokeUseCase,
            fetchSmokeCountListUseCase = fetchSmokeCountListUseCase
        )
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

    @Test
    fun `GIVEN fetch smoke count list is success WHEN add smoke intent is processed THEN it should result with loading and success`() {
        val smokeCountListResult: SmokeCountListResult = mockk()

        coEvery { fetchSmokeCountListUseCase() } answers { smokeCountListResult }

        runBlocking {
            results = processHolder.processIntent(HomeIntent.FetchSmokes)
            assertEquals(HomeResult.Loading, results.first())
            assertEquals(HomeResult.FetchSmokesSuccess(smokeCountListResult), results.last())
        }
    }


    @Test
    fun `GIVEN fetch smoke count list throws exception WHEN add smoke intent is processed THEN it should result with loading and error`() {
        coEvery { fetchSmokeCountListUseCase() } throws (IllegalStateException("User not logged in"))

        runBlocking {
            results = processHolder.processIntent(HomeIntent.FetchSmokes)
            assertEquals(HomeResult.Loading, results.first())
            assertEquals(HomeResult.FetchSmokesError, results.last())
        }
    }
}
