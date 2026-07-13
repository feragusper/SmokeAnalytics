package com.feragusper.smokeanalytics.features.history.presentation

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.features.history.presentation.navigation.HistoryNavigator
import com.feragusper.smokeanalytics.features.history.presentation.process.HistoryProcessHolder
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Clock
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val processHolder: HistoryProcessHolder = mockk(relaxed = true)
    private val navigateUpFn: () -> Unit = mockk(relaxed = true)
    private val navigateToAuthFn: () -> Unit = mockk(relaxed = true)
    private val navigator = HistoryNavigator(
        navigateToAuthentication = navigateToAuthFn,
        navigateUp = navigateUpFn,
    )
    private val now = Clock.System.now()
    private lateinit var viewModel: HistoryViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(result: HistoryResult): HistoryViewModel {
        every { processHolder.processIntent(any()) } returns flowOf(result)
        return HistoryViewModel(processHolder).also {
            it.navigator = navigator
        }
    }

    @Test
    fun `GIVEN loading result THEN displayLoading is true`() = runTest {
        viewModel = createViewModel(HistoryResult.Loading)

        viewModel.onScreenVisible()

        viewModel.states().test {
            awaitItem().displayLoading shouldBeEqualTo true
        }
    }

    @Test
    fun `GIVEN fetch smokes success THEN state has smokes and no error`() = runTest {
        val smokes = listOf(mockk<Smoke>())
        viewModel = createViewModel(
            HistoryResult.FetchSmokesSuccess(
                selectedDate = now,
                smokes = smokes,
                monthCounts = mapOf(1 to 5),
                previousMonthCounts = emptyMap(),
                use24HourClock = true,
            )
        )

        viewModel.onScreenVisible()

        viewModel.states().test {
            val state = awaitItem()
            state.displayLoading shouldBeEqualTo false
            state.error.shouldBeNull()
        }
    }

    @Test
    fun `GIVEN not logged in THEN state has NotLoggedIn error`() = runTest {
        viewModel = createViewModel(HistoryResult.NotLoggedIn(selectedDate = now))

        viewModel.onScreenVisible()

        viewModel.states().test {
            val state = awaitItem()
            state.displayLoading shouldBeEqualTo false
            state.error.shouldNotBeNull()
        }
    }

    @Test
    fun `GIVEN edit in flight THEN pendingSmokeId is set`() = runTest {
        viewModel = createViewModel(HistoryResult.EditSmokeInFlight(id = "123"))

        viewModel.intents().trySend(HistoryIntent.EditSmoke("123", now))

        viewModel.states().test {
            val state = awaitItem()
            state.pendingSmokeId shouldBeEqualTo "123"
            state.pendingAction shouldBeEqualTo HistoryPendingAction.Editing
        }
    }

    @Test
    fun `GIVEN delete in flight THEN pendingSmokeId is set`() = runTest {
        viewModel = createViewModel(HistoryResult.DeleteSmokeInFlight(id = "456"))

        viewModel.intents().trySend(HistoryIntent.DeleteSmoke("456"))

        viewModel.states().test {
            val state = awaitItem()
            state.pendingSmokeId shouldBeEqualTo "456"
            state.pendingAction shouldBeEqualTo HistoryPendingAction.Deleting
        }
    }

    @Test
    fun `GIVEN fetch smokes error THEN error is generic`() = runTest {
        viewModel = createViewModel(HistoryResult.FetchSmokesError)

        viewModel.onScreenVisible()

        viewModel.states().test {
            val state = awaitItem()
            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo HistoryResult.Error.Generic
        }
    }

    @Test
    fun `GIVEN navigate up result THEN navigator is called`() = runTest {
        viewModel = createViewModel(HistoryResult.NavigateUp)

        viewModel.intents().trySend(HistoryIntent.NavigateUp)

        viewModel.states().test {
            awaitItem()
            verify(exactly = 1) { navigateUpFn() }
        }
    }

    @Test
    fun `GIVEN go to authentication result THEN navigator is called`() = runTest {
        viewModel = createViewModel(HistoryResult.GoToAuthentication)

        viewModel.onScreenVisible()

        viewModel.states().test {
            awaitItem()
            verify(exactly = 1) { navigateToAuthFn() }
        }
    }

    @Test
    fun `GIVEN delete success THEN pendingSmokeId is cleared`() = runTest {
        // DeleteSmokeSuccess triggers a re-fetch via intents().trySend,
        // so we provide a second result to avoid infinite loop
        every { processHolder.processIntent(any()) } returnsMany listOf(
            flowOf(HistoryResult.DeleteSmokeSuccess),
            flowOf(HistoryResult.Loading),
        )

        viewModel = HistoryViewModel(processHolder).also { it.navigator = navigator }
        viewModel.intents().trySend(HistoryIntent.DeleteSmoke("any"))

        viewModel.states().test {
            val state = awaitItem()
            state.pendingSmokeId.shouldBeNull()
            state.pendingAction.shouldBeNull()
        }
    }

    @Test
    fun `GIVEN error result THEN state shows error`() = runTest {
        viewModel = createViewModel(HistoryResult.Error.Generic)

        viewModel.onScreenVisible()

        viewModel.states().test {
            val state = awaitItem()
            state.displayLoading shouldBeEqualTo false
            state.error shouldBeEqualTo HistoryResult.Error.Generic
        }
    }
}
