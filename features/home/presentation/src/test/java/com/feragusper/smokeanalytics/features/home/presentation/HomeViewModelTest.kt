package com.feragusper.smokeanalytics.features.home.presentation


import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.features.home.presentation.mvi.compose.HomeViewState
import com.feragusper.smokeanalytics.features.home.presentation.process.HomeProcessHolder
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke
import io.mockk.every
import io.mockk.mockk
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
import java.time.LocalDateTime

class HomeViewModelTest {

    private var processHolder: HomeProcessHolder = mockk()
    private val intentResults = MutableStateFlow<HomeResult>(HomeResult.Loading)
    private lateinit var state: HomeViewState

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        every { processHolder.processIntent(HomeIntent.FetchSmokes) } returns intentResults
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN fetch smokes result WHEN viewmodel is created THEN it shows smoke counts`() =
        runTest {
            val viewModel = HomeViewModel(processHolder)

            val hours = 20L
            val minutes = 10L
            val smokesPerDay = 1
            val smokesPerWeek = 2
            val smokesPerMonth = 3
            val latestSmokes: List<Smoke> = listOf(mockk())

            intentResults.emit(
                HomeResult.FetchSmokesSuccess(
                    mockk<SmokeCountListResult>().apply {
                        every { countByToday } returns smokesPerDay
                        every { countByWeek } returns smokesPerWeek
                        every { countByMonth } returns smokesPerMonth
                        every { todaysSmokes } returns latestSmokes
                        every { timeSinceLastCigarette } returns (hours to minutes)
                    }
                )
            )
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo null
            state.smokesPerDay shouldBeEqualTo smokesPerDay
            state.smokesPerWeek shouldBeEqualTo smokesPerWeek
            state.smokesPerMonth shouldBeEqualTo smokesPerMonth
            state.latestSmokes shouldBeEqualTo latestSmokes
            state.timeSinceLastCigarette shouldBeEqualTo (hours to minutes)
        }

    @Test
    fun `GIVEN update time since last cigarette result WHEN viewmodel is created THEN it shows smoke counts`() =
        runTest {
            val viewModel = HomeViewModel(processHolder)
            val timeSinceLastCigarette: Pair<Long, Long> = mockk()

            intentResults.emit(HomeResult.UpdateTimeSinceLastCigarette(timeSinceLastCigarette))
            state = viewModel.states().first()

            state.timeSinceLastCigarette shouldBeEqualTo timeSinceLastCigarette
        }

    @Test
    fun `GIVEN fetch smokes error WHEN viewmodel is created THEN it shows fetch smokes error`() =
        runTest {
            every { processHolder.processIntent(HomeIntent.AddSmoke) } returns intentResults

            val viewModel = HomeViewModel(processHolder)

            intentResults.emit(HomeResult.FetchSmokesError)

            viewModel.intents().trySend(HomeIntent.AddSmoke)
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
        }

    @Test
    fun `GIVEN loading result WHEN viewmodel is created THEN it hides loading`() = runTest {
        every { processHolder.processIntent(HomeIntent.AddSmoke) } returns intentResults

        val viewModel = HomeViewModel(processHolder)

        intentResults.emit(HomeResult.Loading)

        viewModel.intents().trySend(HomeIntent.AddSmoke)
        state = viewModel.states().first()

        state.displayLoading shouldBeEqualTo true
    }

    @Test
    fun `GIVEN add smoke success and fetch smokes success WHEN add smoke is sent THEN it hides loading and shows success`() =
        runTest {
            every { processHolder.processIntent(HomeIntent.AddSmoke) } returns intentResults

            val viewModel = HomeViewModel(processHolder)

            val hours = 20L
            val minutes = 10L
            val smokesPerDay = 1
            val smokesPerWeek = 2
            val smokesPerMonth = 3
            val latestSmokes: List<Smoke> = listOf(mockk())

            intentResults.emit(HomeResult.AddSmokeSuccess)

            viewModel.intents().trySend(HomeIntent.AddSmoke)

            intentResults.emit(
                HomeResult.FetchSmokesSuccess(
                    mockk<SmokeCountListResult>().apply {
                        every { countByToday } returns smokesPerDay
                        every { countByWeek } returns smokesPerWeek
                        every { countByMonth } returns smokesPerMonth
                        every { todaysSmokes } returns latestSmokes
                        every { timeSinceLastCigarette } returns (hours to minutes)
                    }
                )
            )
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo null
            state.smokesPerDay shouldBeEqualTo smokesPerDay
            state.smokesPerWeek shouldBeEqualTo smokesPerWeek
            state.smokesPerMonth shouldBeEqualTo smokesPerMonth
            state.latestSmokes shouldBeEqualTo latestSmokes
            state.timeSinceLastCigarette shouldBeEqualTo (hours to minutes)
        }

    @Test
    fun `GIVEN edit smoke success and fetch smokes success WHEN edit smoke is sent THEN it hides loading and shows success`() =
        runTest {
            val id = "123"
            val date: LocalDateTime = mockk()
            every {
                processHolder.processIntent(
                    HomeIntent.EditSmoke(
                        id = id,
                        date = date
                    )
                )
            } returns intentResults

            val viewModel = HomeViewModel(processHolder)

            val hours = 20L
            val minutes = 10L
            val smokesPerDay = 1
            val smokesPerWeek = 2
            val smokesPerMonth = 3
            val latestSmokes: List<Smoke> = listOf(mockk())

            intentResults.emit(HomeResult.EditSmokeSuccess)

            viewModel.intents().trySend(
                HomeIntent.EditSmoke(
                    id = id,
                    date = date
                )
            )

            intentResults.emit(
                HomeResult.FetchSmokesSuccess(
                    mockk<SmokeCountListResult>().apply {
                        every { countByToday } returns smokesPerDay
                        every { countByWeek } returns smokesPerWeek
                        every { countByMonth } returns smokesPerMonth
                        every { todaysSmokes } returns latestSmokes
                        every { timeSinceLastCigarette } returns (hours to minutes)
                    }
                )
            )
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo null
            state.smokesPerDay shouldBeEqualTo smokesPerDay
            state.smokesPerWeek shouldBeEqualTo smokesPerWeek
            state.smokesPerMonth shouldBeEqualTo smokesPerMonth
            state.latestSmokes shouldBeEqualTo latestSmokes
            state.timeSinceLastCigarette shouldBeEqualTo (hours to minutes)
        }

    @Test
    fun `GIVEN delete smoke success and fetch smokes success WHEN delete smoke is sent THEN it hides loading and shows success`() =
        runTest {
            val id = "123"
            every { processHolder.processIntent(HomeIntent.DeleteSmoke(id)) } returns intentResults

            val viewModel = HomeViewModel(processHolder)

            val hours = 20L
            val minutes = 10L
            val smokesPerDay = 1
            val smokesPerWeek = 2
            val smokesPerMonth = 3
            val latestSmokes: List<Smoke> = listOf(mockk())

            intentResults.emit(HomeResult.EditSmokeSuccess)

            viewModel.intents().trySend(HomeIntent.DeleteSmoke(id))

            intentResults.emit(
                HomeResult.FetchSmokesSuccess(
                    mockk<SmokeCountListResult>().apply {
                        every { countByToday } returns smokesPerDay
                        every { countByWeek } returns smokesPerWeek
                        every { countByMonth } returns smokesPerMonth
                        every { todaysSmokes } returns latestSmokes
                        every { timeSinceLastCigarette } returns (hours to minutes)
                    }
                )
            )
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo null
            state.smokesPerDay shouldBeEqualTo smokesPerDay
            state.smokesPerWeek shouldBeEqualTo smokesPerWeek
            state.smokesPerMonth shouldBeEqualTo smokesPerMonth
            state.latestSmokes shouldBeEqualTo latestSmokes
            state.timeSinceLastCigarette shouldBeEqualTo (hours to minutes)
        }

    @Test
    fun `GIVEN add smoke error result WHEN add smoke is sent THEN it hides loading and shows error`() =
        runTest {
            every { processHolder.processIntent(HomeIntent.AddSmoke) } returns intentResults

            val viewModel = HomeViewModel(processHolder)

            intentResults.emit(HomeResult.Error.Generic)

            viewModel.intents().trySend(HomeIntent.AddSmoke)
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo HomeResult.Error.Generic
        }

    @Test
    fun `GIVEN edit smoke error result WHEN edit smoke is sent THEN it hides loading and shows error`() =
        runTest {
            val id = "123"
            val date: LocalDateTime = mockk()
            every {
                processHolder.processIntent(
                    HomeIntent.EditSmoke(
                        id = id,
                        date = date
                    )
                )
            } returns intentResults

            val viewModel = HomeViewModel(processHolder)

            intentResults.emit(HomeResult.Error.Generic)

            viewModel.intents().trySend(HomeIntent.EditSmoke(id, date))
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo HomeResult.Error.Generic
        }

    @Test
    fun `GIVEN delete smoke error result WHEN delete smoke is sent THEN it hides loading and shows error`() =
        runTest {
            val id = "123"
            every { processHolder.processIntent(HomeIntent.DeleteSmoke(id)) } returns intentResults

            val viewModel = HomeViewModel(processHolder)

            intentResults.emit(HomeResult.Error.Generic)

            viewModel.intents().trySend(HomeIntent.DeleteSmoke(id))
            state = viewModel.states().first()

            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo HomeResult.Error.Generic
        }

}
