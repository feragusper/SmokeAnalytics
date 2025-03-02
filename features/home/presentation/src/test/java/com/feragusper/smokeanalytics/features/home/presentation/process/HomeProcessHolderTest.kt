package com.feragusper.smokeanalytics.features.home.presentation.process

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.timeElapsedSinceNow
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class HomeProcessHolderTest {

    private lateinit var processHolder: HomeProcessHolder

    private val addSmokeUseCase: AddSmokeUseCase = mockk()
    private val editSmokeUseCase: EditSmokeUseCase = mockk()
    private val deleteSmokeUseCase: DeleteSmokeUseCase = mockk()
    private val fetchSmokeCountListUseCase: FetchSmokeCountListUseCase = mockk()
    private val fetchSessionUseCase: FetchSessionUseCase = mockk()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        processHolder = HomeProcessHolder(
            addSmokeUseCase = addSmokeUseCase,
            editSmokeUseCase = editSmokeUseCase,
            deleteSmokeUseCase = deleteSmokeUseCase,
            fetchSmokeCountListUseCase = fetchSmokeCountListUseCase,
            fetchSessionUseCase = fetchSessionUseCase
        )
    }

    @Nested
    @DisplayName("GIVEN user is logged in")
    inner class UserIsLoggedIn {
        @BeforeEach
        fun setUp() {
            coEvery { fetchSessionUseCase() } returns mockk<Session.LoggedIn>()
        }

        @Test
        fun `WHEN fetching smoke count list THEN it returns success`() = runTest {
            val smokeCountListResult: SmokeCountListResult = mockk()
            coEvery { fetchSmokeCountListUseCase() } returns smokeCountListResult

            processHolder.processIntent(HomeIntent.FetchSmokes).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.FetchSmokesSuccess(smokeCountListResult)
                awaitComplete()
            }
        }

        @Test
        fun `WHEN ticking time since last cigarette THEN it updates time`() = runTest {
            val date: LocalDateTime = mockk()
            val lastSmoke: Smoke = mockk { every { this@mockk.date } returns date }
            val timeElapsedSinceNow: Pair<Long, Long> = mockk()
            mockkStatic(LocalDateTime::timeElapsedSinceNow)
            every { date.timeElapsedSinceNow() } returns timeElapsedSinceNow

            processHolder.processIntent(HomeIntent.TickTimeSinceLastCigarette(lastSmoke)).test {
                awaitItem() shouldBeEqualTo HomeResult.UpdateTimeSinceLastCigarette(
                    timeElapsedSinceNow
                )
                awaitComplete()
            }
        }

        @Test
        fun `WHEN adding smoke THEN it returns success`() = runTest {
            coEvery { addSmokeUseCase.invoke(any()) } returns Unit

            processHolder.processIntent(HomeIntent.AddSmoke).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.AddSmokeSuccess
                awaitComplete()
            }
        }

        @Test
        fun `WHEN editing smoke THEN it returns success`() = runTest {
            val id = "id"
            val date: LocalDateTime = mockk()
            coEvery { editSmokeUseCase(id, date) } just Runs

            processHolder.processIntent(HomeIntent.EditSmoke(id, date)).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.EditSmokeSuccess
                awaitComplete()
            }
        }

        @Test
        fun `WHEN deleting smoke THEN it returns success`() = runTest {
            val id = "id"
            coEvery { deleteSmokeUseCase(id) } just Runs

            processHolder.processIntent(HomeIntent.DeleteSmoke(id)).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.DeleteSmokeSuccess
                awaitComplete()
            }
        }

        @Test
        fun `WHEN adding smoke fails THEN it returns error`() = runTest {
            coEvery { addSmokeUseCase() } throws IllegalStateException("Error")

            processHolder.processIntent(HomeIntent.AddSmoke).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.Error.Generic
                awaitComplete()
            }
        }

        @Test
        fun `WHEN editing smoke fails THEN it returns error`() = runTest {
            val id = "id"
            val date: LocalDateTime = mockk()
            coEvery { editSmokeUseCase(id, date) } throws IllegalStateException("Error")

            processHolder.processIntent(HomeIntent.EditSmoke(id, date)).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.Error.Generic
                awaitComplete()
            }
        }

        @Test
        fun `WHEN deleting smoke fails THEN it returns error`() = runTest {
            val id = "id"
            coEvery { deleteSmokeUseCase(id) } throws IllegalStateException("Error")

            processHolder.processIntent(HomeIntent.DeleteSmoke(id)).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.Error.Generic
                awaitComplete()
            }
        }
    }

    @Nested
    @DisplayName("GIVEN user is not logged in")
    inner class UserIsNotLoggedIn {
        @BeforeEach
        fun setUp() {
            coEvery { fetchSessionUseCase() } returns mockk<Session.Anonymous>()
        }

        @Test
        fun `WHEN adding smoke THEN it returns not logged in error`() = runTest {
            processHolder.processIntent(HomeIntent.AddSmoke).test {
                awaitItem() shouldBeEqualTo HomeResult.Error.NotLoggedIn
                awaitItem() shouldBeEqualTo HomeResult.GoToAuthentication
                awaitComplete()
            }
        }

        @Test
        fun `WHEN fetching smoke count list THEN it returns not logged in error`() = runTest {
            processHolder.processIntent(HomeIntent.FetchSmokes).test {
                awaitItem() shouldBeEqualTo HomeResult.NotLoggedIn
                awaitComplete()
            }
        }
    }
}
