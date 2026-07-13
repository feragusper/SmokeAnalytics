package com.feragusper.smokeanalytics.features.home.presentation.process

import com.feragusper.smokeanalytics.libraries.architecture.domain.NoOpAnalyticsTracker

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.libraries.architecture.domain.Coordinate
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationTrackingAvailability
import com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetRefreshService
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.SetSmokeRelationshipUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.SyncWithWearUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.Craving
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.AddCravingUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.FetchActiveCravingUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.FetchCravingsUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.ResolveCravingUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlin.time.Duration.Companion.minutes
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Clock
import kotlinx.datetime.Instant
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeProcessHolderTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var processHolder: HomeProcessHolder

    private val addSmokeUseCase: AddSmokeUseCase = mockk()
    private val editSmokeUseCase: EditSmokeUseCase = mockk()
    private val deleteSmokeUseCase: DeleteSmokeUseCase = mockk()
    private val setSmokeRelationshipUseCase: SetSmokeRelationshipUseCase = mockk()
    private val fetchSmokeCountListUseCase: FetchSmokeCountListUseCase = mockk()
    private val fetchSmokesUseCase: FetchSmokesUseCase = mockk()
    private val fetchSessionUseCase: FetchSessionUseCase = mockk()
    private val syncWithWearUseCase: SyncWithWearUseCase = mockk()
    private val fetchUserPreferencesUseCase: FetchUserPreferencesUseCase = mockk()
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase = mockk()
    private val locationCaptureService: LocationCaptureService = mockk()
    private val widgetRefreshService: WidgetRefreshService = mockk()
    private val addCravingUseCase: AddCravingUseCase = mockk()
    private val fetchActiveCravingUseCase: FetchActiveCravingUseCase = mockk()
    private val fetchCravingsUseCase: FetchCravingsUseCase = mockk()
    private val resolveCravingUseCase: ResolveCravingUseCase = mockk()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        processHolder = HomeProcessHolder(
            addSmokeUseCase = addSmokeUseCase,
            editSmokeUseCase = editSmokeUseCase,
            deleteSmokeUseCase = deleteSmokeUseCase,
            setSmokeRelationshipUseCase = setSmokeRelationshipUseCase,
            fetchSmokeCountListUseCase = fetchSmokeCountListUseCase,
            fetchSmokesUseCase = fetchSmokesUseCase,
            fetchSessionUseCase = fetchSessionUseCase,
            syncWithWearUseCase = syncWithWearUseCase,
            fetchUserPreferencesUseCase = fetchUserPreferencesUseCase,
            updateUserPreferencesUseCase = updateUserPreferencesUseCase,
            locationCaptureService = locationCaptureService,
            widgetRefreshService = widgetRefreshService,
            addCravingUseCase = addCravingUseCase,
            fetchActiveCravingUseCase = fetchActiveCravingUseCase,
            fetchCravingsUseCase = fetchCravingsUseCase,
            resolveCravingUseCase = resolveCravingUseCase,
            analyticsTracker = NoOpAnalyticsTracker,
        )

        coEvery { fetchActiveCravingUseCase() } returns null
        coEvery { fetchCravingsUseCase.invoke(any(), any()) } returns emptyList()

        coEvery { syncWithWearUseCase.invoke() } just Runs
        coEvery { fetchUserPreferencesUseCase() } returns UserPreferences()
        coEvery { updateUserPreferencesUseCase.invoke(any()) } just Runs
        coEvery { locationCaptureService.locationTrackingAvailability(any()) } answers {
            val preferenceEnabled = firstArg<Boolean>()
            LocationTrackingAvailability(
                preferenceEnabled = preferenceEnabled,
                permissionGranted = preferenceEnabled,
                providerEnabled = preferenceEnabled,
            )
        }
        coEvery { locationCaptureService.captureCurrentLocation() } returns null
        coEvery { fetchSmokeCountListUseCase.invoke(any(), any()) } returns SmokeCountListResult(emptyList(), 0, 0, null)
        coEvery { fetchSmokesUseCase.invoke(any(), any()) } returns emptyList()
        coEvery { widgetRefreshService.refreshHomeSnapshot(any()) } just Runs
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
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
            coEvery { addSmokeUseCase.invoke(any()) } returns "smoke-id"

            processHolder.processIntent(HomeIntent.AddSmoke).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.AddSmokeSuccess("smoke-id")
                coVerify(exactly = 1) { syncWithWearUseCase.invoke() }
                awaitComplete()
            }
        }

        @Test
        fun `WHEN tracking a craving and it is already a good time THEN no wait is needed`() = runTest {
            // No active goal -> the calculator says it's fine to smoke now.
            coEvery { fetchUserPreferencesUseCase() } returns UserPreferences()

            processHolder.processIntent(HomeIntent.TrackCraving).test {
                awaitItem() shouldBeEqualTo HomeResult.CravingNoWaitNeeded
                awaitComplete()
            }
        }

        @Test
        fun `WHEN tracking a craving before the goal gap THEN a pending craving is created`() = runTest {
            val lastSmoke = Smoke(id = "s1", date = Clock.System.now() - 10.minutes)
            coEvery { fetchUserPreferencesUseCase() } returns UserPreferences(
                activeGoal = SmokingGoal.MindfulGap(targetMinutes = 60),
            )
            coEvery { fetchSmokeCountListUseCase.invoke(any(), any()) } returns
                SmokeCountListResult(emptyList(), 0, 0, lastSmoke)
            val created = Craving(id = "c1", createdAt = Clock.System.now())
            coEvery { addCravingUseCase.invoke(any(), any()) } returns created

            processHolder.processIntent(HomeIntent.TrackCraving).test {
                awaitItem() shouldBeEqualTo HomeResult.CravingTracked(created)
                coVerify(exactly = 1) { addCravingUseCase.invoke(any(), any()) }
                awaitComplete()
            }
        }

        @Test
        fun `WHEN resolving a craving as resisted THEN it awards points without logging a smoke`() = runTest {
            val craving = Craving(id = "c1", createdAt = Clock.System.now() - 30.minutes)
            coEvery { resolveCravingUseCase.invoke(craving, CravingOutcome.RESISTED, any()) } returns 16

            processHolder.processIntent(
                HomeIntent.ResolveCraving(craving = craving, smoked = false),
            ).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.CravingResolved(CravingOutcome.RESISTED, 16)
                coVerify(exactly = 0) { addSmokeUseCase.invoke() }
                awaitComplete()
            }
        }

        @Test
        fun `WHEN resolving a craving as smoked after the wait THEN it postpones and logs the smoke`() = runTest {
            val craving = Craving(
                id = "c1",
                createdAt = Clock.System.now() - 60.minutes,
                targetAt = Clock.System.now() - 1.minutes,
            )
            coEvery { addSmokeUseCase.invoke(any(), any()) } returns "smoke-id"
            coEvery { resolveCravingUseCase.invoke(craving, CravingOutcome.POSTPONED, any()) } returns 12

            processHolder.processIntent(
                HomeIntent.ResolveCraving(craving = craving, smoked = true),
            ).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.CravingResolved(CravingOutcome.POSTPONED, 12)
                coVerify(exactly = 1) { addSmokeUseCase.invoke(any(), any()) }
                awaitComplete()
            }
        }

        @Test
        fun `WHEN location preference is enabled and ready THEN smoke is logged first then location attached`() = runTest {
            val locationSlot = slot<GeoPoint>()
            coEvery { fetchUserPreferencesUseCase() } returns UserPreferences(locationTrackingEnabled = true)
            coEvery { locationCaptureService.captureCurrentLocation() } returns Coordinate(12.3, 45.6)
            coEvery { addSmokeUseCase.invoke(any()) } returns "smoke-id"
            coEvery { editSmokeUseCase.invoke(any(), any(), any()) } just Runs

            processHolder.processIntent(HomeIntent.AddSmoke).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                // Logged immediately, before the (potentially slow) location fix.
                awaitItem() shouldBeEqualTo HomeResult.AddSmokeSuccess("smoke-id")
                // Location attached asynchronously via edit.
                awaitItem() shouldBeEqualTo HomeResult.EditSmokeSuccess
                coVerify(exactly = 1) {
                    editSmokeUseCase.invoke("smoke-id", any(), capture(locationSlot))
                }
                locationSlot.captured.latitude shouldBeEqualTo 12.3
                locationSlot.captured.longitude shouldBeEqualTo 45.6
                awaitComplete()
            }
        }

        @Test
        fun `WHEN location preference is enabled but permission is missing THEN add smoke skips location capture`() =
            runTest {
                coEvery { fetchUserPreferencesUseCase() } returns UserPreferences(locationTrackingEnabled = true)
                coEvery { locationCaptureService.locationTrackingAvailability(true) } returns LocationTrackingAvailability(
                    preferenceEnabled = true,
                    permissionGranted = false,
                    providerEnabled = true,
                )
                coEvery { addSmokeUseCase.invoke(any(), null) } returns "smoke-id"

                processHolder.processIntent(HomeIntent.AddSmoke).test {
                    awaitItem() shouldBeEqualTo HomeResult.Loading
                    awaitItem() shouldBeEqualTo HomeResult.AddSmokeSuccess("smoke-id")
                    coVerify(exactly = 0) { locationCaptureService.captureCurrentLocation() }
                    coVerify(exactly = 1) { addSmokeUseCase.invoke(any(), null) }
                    awaitComplete()
                }
            }

        @Test
        fun `WHEN editing smoke THEN it returns success and syncs with Wear`() = runTest {
            val id = "id"
            val date: Instant = Clock.System.now()
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
            coEvery { addSmokeUseCase.invoke(any()) } throws IllegalStateException("Error")

            processHolder.processIntent(HomeIntent.AddSmoke).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.Error.Generic("IllegalStateException: Error")
                coVerify(exactly = 0) { syncWithWearUseCase.invoke() }
                awaitComplete()
            }
        }

        @Test
        fun `WHEN widget refresh fails after adding smoke THEN tracking still succeeds`() = runTest {
            coEvery { addSmokeUseCase.invoke(any()) } returns "smoke-id"
            coEvery { fetchSmokeCountListUseCase.invoke(any(), any()) } throws IllegalStateException("Quota exceeded")

            processHolder.processIntent(HomeIntent.AddSmoke).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.AddSmokeSuccess("smoke-id")
                coVerify(exactly = 1) { syncWithWearUseCase.invoke() }
                awaitComplete()
            }
        }

        @Test
        fun `WHEN goal smoke fetch fails THEN fetch returns error instead of empty progress`() = runTest {
            coEvery { fetchSmokesUseCase.invoke(any(), any()) } throws IllegalStateException("Quota exceeded")

            processHolder.processIntent(HomeIntent.FetchSmokes).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.Error.Generic("IllegalStateException: Quota exceeded")
                awaitComplete()
            }
        }

        @Test
        fun `WHEN starting new day THEN it updates preferences and returns success`() = runTest {
            processHolder.processIntent(HomeIntent.StartNewDay).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.StartNewDaySuccess
                coVerify(exactly = 1) { updateUserPreferencesUseCase.invoke(any()) }
                awaitComplete()
            }
        }

        @Test
        fun `WHEN starting new day fails THEN it returns error`() = runTest {
            coEvery { updateUserPreferencesUseCase.invoke(any()) } throws IllegalStateException("DB error")

            processHolder.processIntent(HomeIntent.StartNewDay).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.Error.Generic("IllegalStateException: DB error")
                awaitComplete()
            }
        }

        @Test
        fun `WHEN ticking time since last cigarette with null smoke THEN it returns zero time`() = runTest {
            processHolder.processIntent(HomeIntent.TickTimeSinceLastCigarette(lastCigarette = null)).test {
                val result = awaitItem()
                result shouldBeInstanceOf HomeResult.UpdateTimeSinceLastCigarette::class
                (result as HomeResult.UpdateTimeSinceLastCigarette).timeSinceLastCigarette shouldBeEqualTo (0L to 0L)
                result.lastSmoke shouldBeEqualTo null
                awaitComplete()
            }
        }

        @Test
        fun `WHEN clicking history THEN it returns GoToHistory`() = runTest {
            processHolder.processIntent(HomeIntent.OnClickHistory).test {
                awaitItem() shouldBeEqualTo HomeResult.GoToHistory
                awaitComplete()
            }
        }

        @Test
        fun `WHEN clicking goals THEN it returns GoToGoals`() = runTest {
            processHolder.processIntent(HomeIntent.OnClickGoals).test {
                awaitItem() shouldBeEqualTo HomeResult.GoToGoals
                awaitComplete()
            }
        }

        @Test
        fun `WHEN refresh fetching smokes THEN it emits RefreshLoading`() = runTest {
            coEvery { fetchSessionUseCase() } returns mockk<Session.LoggedIn> {
                coEvery { user } returns mockk {
                    coEvery { displayName } returns "Test"
                }
            }

            processHolder.processIntent(HomeIntent.RefreshFetchSmokes).test {
                awaitItem() shouldBeEqualTo HomeResult.RefreshLoading
                val result = awaitItem()
                result shouldBeInstanceOf HomeResult.FetchSmokesSuccess::class
                awaitComplete()
            }
        }

        @Test
        fun `WHEN editing smoke fails THEN it returns error`() = runTest {
            val id = "id"
            val date: Instant = Clock.System.now()
            coEvery { editSmokeUseCase(id, date) } throws IllegalStateException("Error")

            processHolder.processIntent(HomeIntent.EditSmoke(id, date)).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.Error.Generic("IllegalStateException: Error")
                coVerify(exactly = 0) { syncWithWearUseCase.invoke() }
                awaitComplete()
            }
        }

        @Test
        fun `WHEN deleting smoke fails THEN it returns error`() = runTest {
            val id = "id"
            coEvery { deleteSmokeUseCase(id) } throws IllegalStateException("Error")

            processHolder.processIntent(HomeIntent.DeleteSmoke(id)).test {
                awaitItem() shouldBeEqualTo HomeResult.Loading
                awaitItem() shouldBeEqualTo HomeResult.Error.Generic("IllegalStateException: Error")
                coVerify(exactly = 0) { syncWithWearUseCase.invoke() }
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
                coVerify(exactly = 0) { syncWithWearUseCase.invoke() }
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
