package com.feragusper.smokeanalytics.features.home.presentation

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.features.home.presentation.process.HomeProcessHolder
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private val processHolder: HomeProcessHolder = mockk()
    private val intentResults = MutableStateFlow<HomeResult>(HomeResult.Loading)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        every { processHolder.processIntent(HomeIntent.FetchSmokes) } returns intentResults
        viewModel = HomeViewModel(processHolder)
    }

    @Test
    fun `GIVEN fetch smokes result WHEN viewmodel is created THEN it shows smoke counts`() =
        runTest {
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

            viewModel.states().test {
                awaitItem().apply {
                    displayLoading shouldBeEqualTo false
                    error shouldBeEqualTo null
                    smokesPerDay shouldBeEqualTo smokesPerDay
                    smokesPerWeek shouldBeEqualTo smokesPerWeek
                    smokesPerMonth shouldBeEqualTo smokesPerMonth
                    latestSmokes shouldBeEqualTo latestSmokes
                    timeSinceLastCigarette shouldBeEqualTo (hours to minutes)
                }
            }
        }

    @Test
    fun `GIVEN update time since last cigarette result WHEN emitted THEN it updates the time`() =
        runTest {
            val timeSinceLastCigarette: Pair<Long, Long> = mockk()
            intentResults.emit(HomeResult.UpdateTimeSinceLastCigarette(timeSinceLastCigarette))

            viewModel.states().test {
                awaitItem().timeSinceLastCigarette shouldBeEqualTo timeSinceLastCigarette
            }
        }

    @Test
    fun `GIVEN fetch smokes error WHEN emitted THEN it shows an error state`() =
        runTest {
            intentResults.emit(HomeResult.FetchSmokesError)

            viewModel.states().test {
                awaitItem().apply {
                    displayLoading shouldBeEqualTo false
                    error shouldBeEqualTo HomeResult.Error.Generic
                }
            }
        }

    @Test
    fun `GIVEN loading result WHEN emitted THEN it shows loading`() =
        runTest {
            intentResults.emit(HomeResult.Loading)

            viewModel.states().test {
                awaitItem().displayLoading shouldBeEqualTo true
            }
        }

    @Test
    fun `GIVEN edit smoke success and fetch smokes success WHEN edit smoke is sent THEN it updates state correctly`() =
        runTest {
            val id = "123"
            val date: LocalDateTime = mockk()

            every {
                processHolder.processIntent(
                    HomeIntent.EditSmoke(
                        id,
                        date
                    )
                )
            } returns intentResults

            viewModel.intents().trySend(HomeIntent.EditSmoke(id, date))
            intentResults.emit(HomeResult.EditSmokeSuccess)

            viewModel.states().test {
                awaitItem().displayLoading shouldBeEqualTo false
            }
        }

    @Test
    fun `GIVEN delete smoke success WHEN delete smoke is sent THEN it updates state correctly`() =
        runTest {
            val id = "123"
            every { processHolder.processIntent(HomeIntent.DeleteSmoke(id)) } returns intentResults

            viewModel.intents().trySend(HomeIntent.DeleteSmoke(id))
            intentResults.emit(HomeResult.DeleteSmokeSuccess)

            viewModel.states().test {
                awaitItem().displayLoading shouldBeEqualTo false
            }
        }

    @Test
    fun `GIVEN add smoke success WHEN add smoke is sent THEN it updates state correctly`() =
        runTest {
            every { processHolder.processIntent(HomeIntent.AddSmoke) } returns intentResults

            viewModel.intents().trySend(HomeIntent.AddSmoke)
            intentResults.emit(HomeResult.AddSmokeSuccess)

            viewModel.states().test {
                awaitItem().displayLoading shouldBeEqualTo false
            }
        }

    @Test
    fun `GIVEN add smoke error WHEN add smoke is sent THEN it shows an error`() =
        runTest {
            every { processHolder.processIntent(HomeIntent.AddSmoke) } returns intentResults

            viewModel.intents().trySend(HomeIntent.AddSmoke)
            intentResults.emit(HomeResult.Error.Generic)

            viewModel.states().test {
                awaitItem().apply {
                    displayLoading shouldBeEqualTo false
                    error shouldBeEqualTo HomeResult.Error.Generic
                }
            }
        }

    @Test
    fun `GIVEN edit smoke error WHEN edit smoke is sent THEN it shows an error`() =
        runTest {
            val id = "123"
            val date: LocalDateTime = mockk()

            every {
                processHolder.processIntent(
                    HomeIntent.EditSmoke(
                        id,
                        date
                    )
                )
            } returns intentResults

            viewModel.intents().trySend(HomeIntent.EditSmoke(id, date))
            intentResults.emit(HomeResult.Error.Generic)

            viewModel.states().test {
                awaitItem().apply {
                    displayLoading shouldBeEqualTo false
                    error shouldBeEqualTo HomeResult.Error.Generic
                }
            }
        }

    @Test
    fun `GIVEN delete smoke error WHEN delete smoke is sent THEN it shows an error`() =
        runTest {
            val id = "123"
            every { processHolder.processIntent(HomeIntent.DeleteSmoke(id)) } returns intentResults

            viewModel.intents().trySend(HomeIntent.DeleteSmoke(id))
            intentResults.emit(HomeResult.Error.Generic)

            viewModel.states().test {
                awaitItem().apply {
                    displayLoading shouldBeEqualTo false
                    error shouldBeEqualTo HomeResult.Error.Generic
                }
            }
        }
}
