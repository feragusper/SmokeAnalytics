package com.feragusper.smokeanalytics.features.history.presentation

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.features.history.presentation.process.HistoryProcessHolder
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private var processHolder: HistoryProcessHolder = mockk()
    private val intentResults = MutableStateFlow<HistoryResult>(HistoryResult.Loading)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        val date: LocalDateTime = mockk()
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns date
        every { processHolder.processIntent(HistoryIntent.FetchSmokes(date)) } returns intentResults
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN fetch smokes result WHEN viewmodel is created THEN it shows smoke list`() = runTest {
        val date: LocalDateTime = mockk()
        val smokeList: List<Smoke> = listOf(mockk<Smoke>())

        every { processHolder.processIntent(HistoryIntent.FetchSmokes(date)) } returns intentResults

        val viewModel = HistoryViewModel(processHolder)

        viewModel.states().test {
            // Ensure that the initial state shows loading
            awaitItem().displayLoading shouldBeEqualTo true

            // Emit success result
            intentResults.emit(HistoryResult.FetchSmokesSuccess(date, smokeList))

            // Verify final state
            awaitItem().apply {
                displayLoading shouldBeEqualTo false
                error shouldBeEqualTo null
                smokes shouldBeEqualTo smokeList
                selectedDate shouldBeEqualTo date
            }
        }
    }

    @Test
    fun `GIVEN fetch smokes error WHEN viewmodel is created THEN it shows fetch smokes error`() =
        runTest {
            val date: LocalDateTime = mockk()

            every { processHolder.processIntent(HistoryIntent.FetchSmokes(date)) } returns intentResults

            val viewModel = HistoryViewModel(processHolder)

            viewModel.states().test {
                // Ensure that the initial state shows loading
                awaitItem().displayLoading shouldBeEqualTo true

                // Emit error result
                intentResults.emit(HistoryResult.FetchSmokesError)

                // Verify final state
                awaitItem().apply {
                    displayLoading shouldBeEqualTo false
                    error shouldBeEqualTo HistoryResult.Error.Generic
                }
            }
        }

    @Test
    fun `GIVEN add smoke success and fetch smokes success WHEN add smoke is sent THEN it hides loading and shows success`() =
        runTest {
            val date: LocalDateTime = mockk()
            val smokeList: List<Smoke> = listOf(mockk<Smoke>())

            every { processHolder.processIntent(HistoryIntent.AddSmoke(date)) } returns intentResults

            val viewModel = HistoryViewModel(processHolder)

            viewModel.intents().trySend(HistoryIntent.AddSmoke(date))

            viewModel.states().test {
                // Expect initial loading state
                awaitItem().displayLoading shouldBeEqualTo true

                // Emit AddSmokeSuccess
                intentResults.emit(HistoryResult.AddSmokeSuccess)

                // Emit FetchSmokesSuccess
                intentResults.emit(
                    HistoryResult.FetchSmokesSuccess(
                        smokes = smokeList,
                        selectedDate = date
                    )
                )

                // Expect final state with updated smoke list and no loading
                awaitItem().let {
                    it.displayLoading shouldBeEqualTo false
                    it.error shouldBeEqualTo null
                    it.smokes shouldBeEqualTo smokeList
                    it.selectedDate shouldBeEqualTo date
                }
            }
        }

    @Test
    fun `GIVEN edit smoke success and fetch smokes success WHEN edit smoke is sent THEN it hides loading and shows success`() =
        runTest {
            val id = "123"
            val date: LocalDateTime = mockk()
            val smokeList: List<Smoke> = listOf(mockk<Smoke>())

            every {
                processHolder.processIntent(HistoryIntent.EditSmoke(id, date))
            } returns intentResults

            val viewModel = HistoryViewModel(processHolder)

            viewModel.intents().trySend(HistoryIntent.EditSmoke(id, date))

            viewModel.states().test {
                // Expect initial loading state
                awaitItem().displayLoading shouldBeEqualTo true

                // Emit EditSmokeSuccess
                intentResults.emit(HistoryResult.EditSmokeSuccess)

                // Emit FetchSmokesSuccess
                intentResults.emit(
                    HistoryResult.FetchSmokesSuccess(
                        smokes = smokeList,
                        selectedDate = date
                    )
                )

                // Expect final state with updated smoke list and no loading
                awaitItem().let {
                    it.displayLoading shouldBeEqualTo false
                    it.error shouldBeEqualTo null
                    it.smokes shouldBeEqualTo smokeList
                    it.selectedDate shouldBeEqualTo date
                }
            }
        }

    @Test
    fun `GIVEN delete smoke success and fetch smokes success WHEN delete smoke is sent THEN it hides loading and shows success`() =
        runTest {
            val id = "123"
            val date: LocalDateTime = mockk()
            val smokeList: List<Smoke> = listOf(mockk<Smoke>())

            every { processHolder.processIntent(HistoryIntent.DeleteSmoke(id)) } returns intentResults

            val viewModel = HistoryViewModel(processHolder)

            viewModel.intents().trySend(HistoryIntent.DeleteSmoke(id))

            viewModel.states().test {
                // Expect initial loading state
                awaitItem().displayLoading shouldBeEqualTo true

                // Emit DeleteSmokeSuccess
                intentResults.emit(HistoryResult.DeleteSmokeSuccess)

                // Emit FetchSmokesSuccess
                intentResults.emit(
                    HistoryResult.FetchSmokesSuccess(
                        smokes = smokeList,
                        selectedDate = date
                    )
                )

                // Expect final state with updated smoke list and no loading
                awaitItem().let {
                    it.displayLoading shouldBeEqualTo false
                    it.error shouldBeEqualTo null
                    it.smokes shouldBeEqualTo smokeList
                    it.selectedDate shouldBeEqualTo date
                }
            }
        }

    @Test
    fun `GIVEN add smoke error result WHEN add smoke is sent THEN it hides loading and shows error`() =
        runTest {
            val date: LocalDateTime = mockk()

            every { processHolder.processIntent(HistoryIntent.AddSmoke(date)) } returns intentResults

            val viewModel = HistoryViewModel(processHolder)

            viewModel.intents().trySend(HistoryIntent.AddSmoke(date))

            viewModel.states().test {
                // Expect initial loading state
                awaitItem().displayLoading shouldBeEqualTo true

                // Emit Error.Generic
                intentResults.emit(HistoryResult.Error.Generic)

                // Expect final state with error set and no loading
                awaitItem().let {
                    it.displayLoading shouldBeEqualTo false
                    it.error shouldBeEqualTo HistoryResult.Error.Generic
                }
            }
        }

    @Test
    fun `GIVEN edit smoke error result WHEN edit smoke is sent THEN it hides loading and shows error`() =
        runTest {
            val id = "123"
            val date: LocalDateTime = mockk()

            every {
                processHolder.processIntent(
                    HistoryIntent.EditSmoke(id, date)
                )
            } returns intentResults

            val viewModel = HistoryViewModel(processHolder)

            viewModel.intents().trySend(HistoryIntent.EditSmoke(id, date))

            viewModel.states().test {
                // Expect initial loading state
                awaitItem().displayLoading shouldBeEqualTo true

                // Emit Error.Generic
                intentResults.emit(HistoryResult.Error.Generic)

                // Expect final state with error set and no loading
                awaitItem().let {
                    it.displayLoading shouldBeEqualTo false
                    it.error shouldBeEqualTo HistoryResult.Error.Generic
                }
            }
        }

    @Test
    fun `GIVEN delete smoke error result WHEN delete smoke is sent THEN it hides loading and shows error`() =
        runTest {
            val id = "123"
            every { processHolder.processIntent(HistoryIntent.DeleteSmoke(id)) } returns intentResults

            val viewModel = HistoryViewModel(processHolder)

            viewModel.intents().trySend(HistoryIntent.DeleteSmoke(id))

            viewModel.states().test {
                // Expect initial loading state
                awaitItem().displayLoading shouldBeEqualTo true

                // Emit error
                intentResults.emit(HistoryResult.Error.Generic)

                // Expect final state with error set and no loading
                awaitItem().let {
                    it.displayLoading shouldBeEqualTo false
                    it.error shouldBeEqualTo HistoryResult.Error.Generic
                }
            }
        }

}
