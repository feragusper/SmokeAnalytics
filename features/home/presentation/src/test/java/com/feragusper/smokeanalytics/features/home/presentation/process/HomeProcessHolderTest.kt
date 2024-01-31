package com.feragusper.smokeanalytics.features.home.presentation.process

import com.feragusper.smokeanalytics.features.home.domain.AddSmokeUseCase
import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.domain.Smoke
import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.timeElapsedSinceNow
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

class HomeProcessHolderTest {

    private lateinit var results: Flow<HomeResult>
    private lateinit var processHolder: HomeProcessHolder

    private var addSmokeUseCase: AddSmokeUseCase = mockk()
    private var fetchSmokeCountListUseCase: FetchSmokeCountListUseCase = mockk()
    private var fetchSessionUseCase: FetchSessionUseCase = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        processHolder = HomeProcessHolder(
            addSmokeUseCase = addSmokeUseCase,
            fetchSmokeCountListUseCase = fetchSmokeCountListUseCase,
            fetchSessionUseCase = fetchSessionUseCase
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    @DisplayName("GIVEN user is logged in")
    inner class UserIsLoggedIn {
        @BeforeEach
        fun setUp() {
            coEvery { fetchSessionUseCase() } answers { mockk<Session.LoggedIn>() }
        }

        @Test
        fun `AND fetch smoke count list is success WHEN add smoke intent is processed THEN it should result with loading and success`() =
            runTest {
                val smokeCountListResult: SmokeCountListResult = mockk()

                coEvery { fetchSmokeCountListUseCase() } answers { smokeCountListResult }

                results = processHolder.processIntent(HomeIntent.FetchSmokes)
                assertEquals(HomeResult.Loading, results.first())
                assertEquals(HomeResult.FetchSmokesSuccess(smokeCountListResult), results.last())
            }

        @Test
        fun `WHEN tick time since last cigarette intent is processed THEN it should result with loading and success`() =
            runTest {
                val date: Date = mockk()
                val lastSmoke: Smoke = mockk<Smoke>().apply {
                    coEvery { this@apply.date } answers { date }
                }
                val timeElapsedSinceNow: Pair<Long, Long> = mockk()
                mockkStatic(Date::timeElapsedSinceNow).apply {
                    coEvery { date.timeElapsedSinceNow() } answers { timeElapsedSinceNow }
                }

                results =
                    processHolder.processIntent(HomeIntent.TickTimeSinceLastCigarette(lastSmoke))
                assertEquals(
                    HomeResult.UpdateTimeSinceLastCigarette(timeElapsedSinceNow),
                    results.first()
                )
            }


        @Test
        fun `AND fetch smoke count list throws exception WHEN add smoke intent is processed THEN it should result with loading and error`() =
            runTest {
                coEvery { fetchSmokeCountListUseCase() } throws (IllegalStateException("User not logged in"))

                results = processHolder.processIntent(HomeIntent.FetchSmokes)
                assertEquals(HomeResult.Loading, results.first())
                assertEquals(HomeResult.FetchSmokesError, results.last())
            }

        @Test
        fun `AND add smoke is success WHEN add smoke intent is processed THEN it should result with loading and success`() =
            runTest {
                coEvery { addSmokeUseCase() } answers {}

                results = processHolder.processIntent(HomeIntent.AddSmoke)
                assertEquals(HomeResult.Loading, results.first())
                assertEquals(HomeResult.AddSmokeSuccess, results.last())
            }


        @Test
        fun `AND add smoke throws exception WHEN add smoke intent is processed THEN it should result with loading and error`() =
            runTest {
                coEvery { addSmokeUseCase() } throws (IllegalStateException("User not logged in"))

                results = processHolder.processIntent(HomeIntent.AddSmoke)
                assertEquals(HomeResult.Loading, results.first())
                assertEquals(HomeResult.Error.Generic, results.last())
            }
    }

    @Nested
    @DisplayName("GIVEN user not is logged in")
    inner class UserIsNotLoggedIn {
        @BeforeEach
        fun setUp() {
            coEvery { fetchSessionUseCase() } answers { mockk<Session.Anonymous>() }
        }

        @Test
        fun `WHEN add smoke intent is processed THEN it should result with not logged in`() =
            runTest {
                results = processHolder.processIntent(HomeIntent.AddSmoke)
                assertEquals(HomeResult.Error.NotLoggedIn, results.first())
                assertEquals(HomeResult.GoToLogin, results.last())
            }

        @Test
        fun `WHEN fetch smoke count list intent is processed THEN it should result with not logged in`() =
            runTest {
                results = processHolder.processIntent(HomeIntent.FetchSmokes)
                assertEquals(HomeResult.NotLoggedIn, results.first())
            }
    }

}
