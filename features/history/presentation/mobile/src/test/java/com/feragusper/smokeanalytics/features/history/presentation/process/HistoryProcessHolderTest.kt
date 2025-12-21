package com.feragusper.smokeanalytics.features.history.presentation.process

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.SyncWithWearUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBe
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
    private val syncWithWearUseCase: SyncWithWearUseCase = mockk()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        processHolder = HistoryProcessHolder(
            addSmokeUseCase,
            editSmokeUseCase,
            deleteSmokeUseCase,
            fetchSmokesUseCase,
            fetchSessionUseCase,
            syncWithWearUseCase
        )

        // Default mock behavior
        coEvery { syncWithWearUseCase.invoke() } just Runs
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
        fun `WHEN adding smoke THEN returns Loading, Success and syncs with Wear`() = runTest {
            val date: LocalDateTime = mockk()
            coEvery { addSmokeUseCase(date) } just Runs

            results = processHolder.processIntent(HistoryIntent.AddSmoke(date))

            results.test {
                awaitItem() shouldBe HistoryResult.Loading
                awaitItem() shouldBe HistoryResult.AddSmokeSuccess
                coVerify(exactly = 1) { syncWithWearUseCase.invoke() }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `WHEN editing smoke THEN returns Loading, Success and syncs with Wear`() = runTest {
            val id = "id"
            val date: LocalDateTime = mockk()
            coEvery { editSmokeUseCase(id, date) } just Runs

            results = processHolder.processIntent(HistoryIntent.EditSmoke(id, date))

            results.test {
                awaitItem() shouldBe HistoryResult.Loading
                awaitItem() shouldBe HistoryResult.EditSmokeSuccess
                coVerify(exactly = 1) { syncWithWearUseCase.invoke() }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `WHEN deleting smoke THEN returns Loading, Success and syncs with Wear`() = runTest {
            val id = "id"
            coEvery { deleteSmokeUseCase(id) } just Runs

            results = processHolder.processIntent(HistoryIntent.DeleteSmoke(id))

            results.test {
                awaitItem() shouldBe HistoryResult.Loading
                awaitItem() shouldBe HistoryResult.DeleteSmokeSuccess
                coVerify(exactly = 1) { syncWithWearUseCase.invoke() }
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
                coVerify(exactly = 0) { syncWithWearUseCase.invoke() } // Ensure sync is not called
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
