package com.feragusper.smokeanalytics.features.home.presentation.process

import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.features.history.presentation.process.HistoryProcessHolder
import com.feragusper.smokeanalytics.libraries.smokes.domain.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.FetchSmokesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
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
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class HistoryProcessHolderTest {

    private lateinit var results: Flow<HistoryResult>
    private lateinit var processHolder: HistoryProcessHolder

    private var addSmokeUseCase: AddSmokeUseCase = mockk()
    private var editSmokeUseCase: EditSmokeUseCase = mockk()
    private var deleteSmokeUseCase: DeleteSmokeUseCase = mockk()
    private var fetchSmokesUseCase: FetchSmokesUseCase = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        processHolder = HistoryProcessHolder(
            addSmokeUseCase = addSmokeUseCase,
            editSmokeUseCase = editSmokeUseCase,
            deleteSmokeUseCase = deleteSmokeUseCase,
            fetchSmokesUseCase = fetchSmokesUseCase,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `AND fetch smoke count list is success WHEN add smoke intent is processed THEN it should result with loading and success`() =
        runTest {
            val smokeList: List<Smoke> = mockk()
            val date: LocalDateTime = mockk()
            coEvery { fetchSmokesUseCase(date) } answers { smokeList }

            results = processHolder.processIntent(HistoryIntent.FetchSmokes(date))
            assertEquals(HistoryResult.Loading, results.first())
            assertEquals(
                HistoryResult.FetchSmokesSuccess(
                    selectedDate = date,
                    smokes = smokeList
                ), results.last()
            )
        }

    @Test
    fun `AND add smoke is success WHEN add smoke intent is processed THEN it should result with loading and success`() =
        runTest {
            val date: LocalDateTime = mockk()
            coEvery { addSmokeUseCase(date) } just Runs

            results = processHolder.processIntent(HistoryIntent.AddSmoke(date))
            assertEquals(HistoryResult.Loading, results.first())
            assertEquals(HistoryResult.AddSmokeSuccess, results.last())
        }

    @Test
    fun `AND edit smoke is success WHEN edit smoke intent is processed THEN it should result with loading and success`() =
        runTest {
            val id = "id"
            val date: LocalDateTime = mockk()
            coEvery {
                editSmokeUseCase(
                    id = id,
                    date = date
                )
            } just Runs

            results = processHolder.processIntent(
                HistoryIntent.EditSmoke(
                    id = id,
                    date = date
                )
            )
            assertEquals(HistoryResult.Loading, results.first())
            assertEquals(HistoryResult.EditSmokeSuccess, results.last())
        }


    @Test
    fun `AND edit smoke throws exception WHEN edit smoke intent is processed THEN it should result with loading and error`() =
        runTest {
            val id = "id"
            val date: LocalDateTime = mockk()
            coEvery {
                editSmokeUseCase(
                    id = id,
                    date = date
                )
            } throws (IllegalStateException("Error"))

            results = processHolder.processIntent(
                HistoryIntent.EditSmoke(
                    id = id,
                    date = date
                )
            )

            assertEquals(HistoryResult.Loading, results.first())
            assertEquals(HistoryResult.Error.Generic, results.last())
        }

    @Test
    fun `AND delete smoke is success WHEN delete smoke intent is processed THEN it should result with loading and success`() =
        runTest {
            val id = "id"
            coEvery { deleteSmokeUseCase(id) } just Runs

            results = processHolder.processIntent(HistoryIntent.DeleteSmoke(id))

            assertEquals(HistoryResult.Loading, results.first())
            assertEquals(HistoryResult.DeleteSmokeSuccess, results.last())
        }


    @Test
    fun `AND delete smoke throws exception WHEN delete smoke intent is processed THEN it should result with loading and error`() =
        runTest {
            val id = "id"
            coEvery { deleteSmokeUseCase(id) } throws (IllegalStateException("Error"))

            results = processHolder.processIntent(HistoryIntent.DeleteSmoke(id))

            assertEquals(HistoryResult.Loading, results.first())
            assertEquals(HistoryResult.Error.Generic, results.last())
        }

}
