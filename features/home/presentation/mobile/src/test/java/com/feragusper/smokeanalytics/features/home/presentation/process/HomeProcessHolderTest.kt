package com.feragusper.smokeanalytics.features.home.presentation.process

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
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
    private val syncWithWearUseCase: SyncWithWearUseCase = mockk()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        processHolder = HomeProcessHolder(
            addSmokeUseCase = addSmokeUseCase,
            editSmokeUseCase = editSmokeUseCase,
            deleteSmokeUseCase = deleteSmokeUseCase,
            fetchSmokeCountListUseCase = fetchSmokeCountListUseCase,
            fetchSessionUseCase = fetchSessionUseCase,
            syncWithWearUseCase = syncWithWearUseCase
        )

        // Default mock behavior for sync
        coEvery { syncWithWearUseCase.invoke() } just Runs
    }

    @Nested
    @DisplayName("GIVEN user is logged in")
    inner class UserIsLoggedIn {
        @BeforeEach
        fun setUp() {
            coEvery { fetchSessionUseCase() } returns mockk<Session.LoggedIn>()
        }

        @Test
        fun `WHEN adding smoke THEN it returns success and syncs with Wear`() = runTest {
            coEvery { addSmokeUseCase.invoke(any()) } just Runs
            coEvery { syncWithWearUseCase.invoke() } just Runs

            processHolder.processIntent(HomeIntent.AddSmoke).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.AddSmokeSuccess
                coVerify(exactly = 1) { syncWithWearUseCase.invoke() }
                awaitComplete()
            }
        }

        @Test
        fun `WHEN editing smoke THEN it returns success and syncs with Wear`() = runTest {
            val id = "id"
            val date: LocalDateTime = mockk()
            coEvery { editSmokeUseCase(id, date) } just Runs

            processHolder.processIntent(HomeIntent.EditSmoke(id, date)).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.EditSmokeSuccess
                coVerify(exactly = 1) { syncWithWearUseCase.invoke() }
                awaitComplete()
            }
        }

        @Test
        fun `WHEN deleting smoke THEN it returns success and syncs with Wear`() = runTest {
            val id = "id"
            coEvery { deleteSmokeUseCase(id) } just Runs

            processHolder.processIntent(HomeIntent.DeleteSmoke(id)).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.DeleteSmokeSuccess
                coVerify(exactly = 1) { syncWithWearUseCase.invoke() }
                awaitComplete()
            }
        }

        @Test
        fun `WHEN adding smoke fails THEN it returns error`() = runTest {
            coEvery { addSmokeUseCase() } throws IllegalStateException("Error")

            processHolder.processIntent(HomeIntent.AddSmoke).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.Error.Generic
                coVerify(exactly = 0) { syncWithWearUseCase.invoke() } // Ensure sync is not called
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
                coVerify(exactly = 0) { syncWithWearUseCase.invoke() } // Ensure sync is not called
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
                coVerify(exactly = 0) { syncWithWearUseCase.invoke() } // Ensure sync is not called
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
                coVerify(exactly = 0) { syncWithWearUseCase.invoke() } // Ensure sync is not called
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
