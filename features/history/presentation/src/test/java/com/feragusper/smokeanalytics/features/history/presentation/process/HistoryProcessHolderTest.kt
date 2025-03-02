package com.feragusper.smokeanalytics.features.history.presentation.process

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryProcessHolderTest {

    private lateinit var processHolder: HistoryProcessHolder
    private lateinit var results: Flow<HistoryResult>

    private val addSmokeUseCase: AddSmokeUseCase = mockk()
    private val editSmokeUseCase: EditSmokeUseCase = mockk()
    private val deleteSmokeUseCase: DeleteSmokeUseCase = mockk()
    private val fetchSmokesUseCase: FetchSmokesUseCase = mockk()
    private val fetchSessionUseCase: FetchSessionUseCase = mockk()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        processHolder = HistoryProcessHolder(
            addSmokeUseCase,
            editSmokeUseCase,
            deleteSmokeUseCase,
            fetchSmokesUseCase,
            fetchSessionUseCase,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    @DisplayName("GIVEN user is logged in")
    inner class UserLoggedIn {

        @BeforeEach
        fun setUp() {
            coEvery { fetchSessionUseCase() } returns mockk<Session.LoggedIn>()
        }

        @Test
        fun `WHEN fetching smoke list THEN returns Loading and Success`() = runTest {
            val date: LocalDateTime = mockk()
            val smokeList: List<Smoke> = mockk()
            coEvery { fetchSmokesUseCase(date) } returns smokeList

            results = processHolder.processIntent(HistoryIntent.FetchSmokes(date))

            results.test {
                awaitItem() shouldBe HistoryResult.Loading
                awaitItem() shouldBeEqualTo HistoryResult.FetchSmokesSuccess(
                    selectedDate = date,
                    smokes = smokeList
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `WHEN adding smoke THEN returns Loading and Success`() = runTest {
            val date: LocalDateTime = mockk()
            coEvery { addSmokeUseCase(date) } just Runs

            results = processHolder.processIntent(HistoryIntent.AddSmoke(date))

            results.test {
                awaitItem() shouldBe HistoryResult.Loading
                awaitItem() shouldBe HistoryResult.AddSmokeSuccess
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `WHEN editing smoke THEN returns Loading and Success`() = runTest {
            val id = "id"
            val date: LocalDateTime = mockk()
            coEvery { editSmokeUseCase(id, date) } just Runs

            results = processHolder.processIntent(HistoryIntent.EditSmoke(id, date))

            results.test {
                awaitItem() shouldBe HistoryResult.Loading
                awaitItem() shouldBe HistoryResult.EditSmokeSuccess
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `WHEN editing smoke fails THEN returns Loading and Generic Error`() = runTest {
            val id = "id"
            val date: LocalDateTime = mockk()
            coEvery { editSmokeUseCase(id, date) } throws IllegalStateException("Error")

            results = processHolder.processIntent(HistoryIntent.EditSmoke(id, date))

            results.test {
                awaitItem() shouldBe HistoryResult.Loading
                awaitItem() shouldBe HistoryResult.Error.Generic
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `WHEN deleting smoke THEN returns Loading and Success`() = runTest {
            val id = "id"
            coEvery { deleteSmokeUseCase(id) } just Runs

            results = processHolder.processIntent(HistoryIntent.DeleteSmoke(id))

            results.test {
                awaitItem() shouldBe HistoryResult.Loading
                awaitItem() shouldBe HistoryResult.DeleteSmokeSuccess
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `WHEN deleting smoke fails THEN returns Loading and Generic Error`() = runTest {
            val id = "id"
            coEvery { deleteSmokeUseCase(id) } throws IllegalStateException("Error")

            results = processHolder.processIntent(HistoryIntent.DeleteSmoke(id))

            results.test {
                awaitItem() shouldBe HistoryResult.Loading
                awaitItem() shouldBe HistoryResult.Error.Generic
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("GIVEN user is not logged in")
    inner class UserNotLoggedIn {

        @BeforeEach
        fun setUp() {
            coEvery { fetchSessionUseCase() } returns mockk<Session.Anonymous>()
        }

        @Test
        fun `WHEN adding smoke THEN returns NotLoggedIn Error and GoToAuthentication`() = runTest {
            results = processHolder.processIntent(HistoryIntent.AddSmoke(mockk()))

            results.test {
                awaitItem() shouldBe HistoryResult.Error.NotLoggedIn
                awaitItem() shouldBe HistoryResult.GoToAuthentication
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `WHEN fetching smoke list THEN returns NotLoggedIn`() = runTest {
            val date: LocalDateTime = mockk()
            results = processHolder.processIntent(HistoryIntent.FetchSmokes(date))

            results.test {
                awaitItem() shouldBeEqualTo HistoryResult.NotLoggedIn(date)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
