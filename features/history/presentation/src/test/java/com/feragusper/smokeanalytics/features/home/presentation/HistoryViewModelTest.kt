package com.feragusper.smokeanalytics.features.home.presentation


import com.feragusper.smokeanalytics.features.history.presentation.HistoryViewModel
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryViewState
import com.feragusper.smokeanalytics.features.history.presentation.process.HistoryProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.dateFormatted
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.withTimeZeroed
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date

class HistoryViewModelTest {

    private var processHolder: HistoryProcessHolder = mockk()
    private val intentResults = MutableStateFlow<HistoryResult>(HistoryResult.Loading)
    private lateinit var state: HistoryViewState

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        every { processHolder.processIntent(HistoryIntent.FetchSmokes) } returns intentResults
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN fetch smokes result WHEN viewmodel is created THEN it shows smoke list`() =
        runTest {
            val viewModel = HistoryViewModel(processHolder)

            mockkStatic(Date::dateFormatted)
            val dateWithoutTime: Date = mockk()
            val smoke = mockk<Smoke>().apply {
                every { this@apply.date } returns mockk<Date>().apply {
                    every { withTimeZeroed() } returns dateWithoutTime
                }
            }
            val smokeList: List<Smoke> = listOf(smoke)

            intentResults.emit(
                HistoryResult.FetchSmokesSuccess(smokeList)
            )
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo null
            state.smokes shouldBeEqualTo mapOf(dateWithoutTime to smokeList)
        }

    @Test
    fun `GIVEN fetch smokes error WHEN viewmodel is created THEN it shows fetch smokes error`() =
        runTest {
            val date: Date = mockk()
            every { processHolder.processIntent(HistoryIntent.AddSmoke(date)) } returns intentResults

            val viewModel = HistoryViewModel(processHolder)

            intentResults.emit(HistoryResult.FetchSmokesError)

            viewModel.intents().trySend(HistoryIntent.AddSmoke(date))
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
        }

    @Test
    fun `GIVEN loading result WHEN viewmodel is created THEN it hides loading`() = runTest {
        val date: Date = mockk()
        every { processHolder.processIntent(HistoryIntent.AddSmoke(date)) } returns intentResults

        val viewModel = HistoryViewModel(processHolder)

        intentResults.emit(HistoryResult.Loading)

        viewModel.intents().trySend(HistoryIntent.AddSmoke(date))
        state = viewModel.states().first()

        state.displayLoading shouldBeEqualTo true
    }

    @Test
    fun `GIVEN add smoke success and fetch smokes success WHEN add smoke is sent THEN it hides loading and shows success`() =
        runTest {
            val date: Date = mockk()
            every { processHolder.processIntent(HistoryIntent.AddSmoke(date)) } returns intentResults

            val viewModel = HistoryViewModel(processHolder)

            val dateWithoutTime: Date = mockk()
            mockkStatic(Date::dateFormatted)
            val smoke = mockk<Smoke>().apply {
                every { this@apply.date } returns mockk<Date>().apply {
                    every { withTimeZeroed() } returns dateWithoutTime
                }
            }
            val smokeList: List<Smoke> = listOf(smoke)

            intentResults.emit(HistoryResult.AddSmokeSuccess)

            viewModel.intents().trySend(HistoryIntent.AddSmoke(date))

            intentResults.emit(HistoryResult.FetchSmokesSuccess(smokeList))
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo null
            state.smokes shouldBeEqualTo mapOf(dateWithoutTime to smokeList)
        }

    @Test
    fun `GIVEN edit smoke success and fetch smokes success WHEN edit smoke is sent THEN it hides loading and shows success`() =
        runTest {
            val id = "123"
            val date: Date = mockk()
            every {
                processHolder.processIntent(
                    HistoryIntent.EditSmoke(
                        id = id,
                        date = date
                    )
                )
            } returns intentResults

            val viewModel = HistoryViewModel(processHolder)

            val dateWithoutTime: Date = mockk()
            mockkStatic(Date::dateFormatted)
            val smoke = mockk<Smoke>().apply {
                every { this@apply.date } returns mockk<Date>().apply {
                    every { withTimeZeroed() } returns dateWithoutTime
                }
            }
            val smokeList: List<Smoke> = listOf(smoke)

            intentResults.emit(HistoryResult.EditSmokeSuccess)

            viewModel.intents().trySend(
                HistoryIntent.EditSmoke(
                    id = id,
                    date = date
                )
            )

            intentResults.emit(HistoryResult.FetchSmokesSuccess(smokeList))
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo null
            state.smokes shouldBeEqualTo mapOf(dateWithoutTime to smokeList)
        }

    @Test
    fun `GIVEN delete smoke success and fetch smokes success WHEN delete smoke is sent THEN it hides loading and shows success`() =
        runTest {
            val id = "123"
            every { processHolder.processIntent(HistoryIntent.DeleteSmoke(id)) } returns intentResults

            val viewModel = HistoryViewModel(processHolder)

            mockkStatic(Date::dateFormatted)
            val date: Date = mockk()
            val smoke = mockk<Smoke>().apply {
                every { this@apply.date } returns mockk<Date>().apply {
                    every { withTimeZeroed() } returns date
                }
            }
            val smokeList: List<Smoke> = listOf(smoke)

            intentResults.emit(HistoryResult.DeleteSmokeSuccess)

            viewModel.intents().trySend(HistoryIntent.DeleteSmoke(id))

            intentResults.emit(HistoryResult.FetchSmokesSuccess(smokeList))
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo null
            state.smokes shouldBeEqualTo mapOf(date to smokeList)
        }

    @Test
    fun `GIVEN add smoke error result WHEN add smoke is sent THEN it hides loading and shows error`() =
        runTest {
            val date: Date = mockk()
            every { processHolder.processIntent(HistoryIntent.AddSmoke(date)) } returns intentResults

            val viewModel = HistoryViewModel(processHolder)

            intentResults.emit(HistoryResult.Error.Generic)

            viewModel.intents().trySend(HistoryIntent.AddSmoke(date))
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo HistoryResult.Error.Generic
        }

    @Test
    fun `GIVEN edit smoke error result WHEN edit smoke is sent THEN it hides loading and shows error`() =
        runTest {
            val id = "123"
            val date: Date = mockk()
            every {
                processHolder.processIntent(
                    HistoryIntent.EditSmoke(
                        id = id,
                        date = date
                    )
                )
            } returns intentResults

            val viewModel = HistoryViewModel(processHolder)

            intentResults.emit(HistoryResult.Error.Generic)

            viewModel.intents().trySend(HistoryIntent.EditSmoke(id, date))
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo HistoryResult.Error.Generic
        }

    @Test
    fun `GIVEN delete smoke error result WHEN delete smoke is sent THEN it hides loading and shows error`() =
        runTest {
            val id = "123"
            every { processHolder.processIntent(HistoryIntent.DeleteSmoke(id)) } returns intentResults

            val viewModel = HistoryViewModel(processHolder)

            intentResults.emit(HistoryResult.Error.Generic)

            viewModel.intents().trySend(HistoryIntent.DeleteSmoke(id))
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo HistoryResult.Error.Generic
        }

}
