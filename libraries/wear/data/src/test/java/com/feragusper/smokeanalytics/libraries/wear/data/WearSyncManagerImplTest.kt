package com.feragusper.smokeanalytics.libraries.wear.data

import android.content.Context
import com.feragusper.smokeanalytics.libraries.architecture.common.coroutines.DispatcherProvider
import com.feragusper.smokeanalytics.libraries.architecture.domain.Coordinate
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationTrackingAvailability
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferencesRepository
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeCount
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WearSyncManagerImplTest {

    private val smokeRepository: SmokeRepository = mockk(relaxed = true)
    private val userPreferencesRepository: UserPreferencesRepository = mockk()
    private val locationCaptureService: LocationCaptureService = mockk()
    private val context: Context = mockk(relaxed = true)
    private val dispatcherProvider: DispatcherProvider = mockk {
        every { io() } returns UnconfinedTestDispatcher()
    }

    private val mobile = WearSyncManagerImpl(context, dispatcherProvider).Mobile(
        smokeRepository = smokeRepository,
        userPreferencesRepository = userPreferencesRepository,
        locationCaptureService = locationCaptureService,
    )

    @BeforeEach
    fun setUp() {
        // syncWithWear() (invoked after addSmoke) touches the Wearable DataClient and
        // builds a PutDataMapRequest, both of which rely on Android framework statics.
        mockkStatic(Wearable::class)
        every { Wearable.getDataClient(any<Context>()) } returns mockk(relaxed = true)
        every { Wearable.getMessageClient(any<Context>()) } returns mockk(relaxed = true)
        mockkStatic(PutDataMapRequest::class)
        every { PutDataMapRequest.create(any()) } returns mockk(relaxed = true)
        coEvery { smokeRepository.fetchSmokeCount(any(), any()) } returns SmokeCount(
            today = emptyList(),
            week = 0,
            month = 0,
            lastSmoke = null,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Wearable::class)
        unmockkStatic(PutDataMapRequest::class)
    }

    @Test
    fun `GIVEN location tracking ready WHEN add smoke request THEN smoke is saved with location`() =
        runTest {
            coEvery { userPreferencesRepository.fetch() } returns
                UserPreferences(locationTrackingEnabled = true)
            coEvery { locationCaptureService.locationTrackingAvailability(true) } returns
                LocationTrackingAvailability(
                    preferenceEnabled = true,
                    permissionGranted = true,
                    providerEnabled = true,
                )
            coEvery { locationCaptureService.captureCurrentLocation() } returns
                Coordinate(latitude = 12.34, longitude = 56.78)

            mobile.handleWearRequest(WearPaths.ADD_SMOKE)

            coVerify {
                smokeRepository.addSmoke(any(), GeoPoint(latitude = 12.34, longitude = 56.78))
            }
        }

    @Test
    fun `GIVEN location tracking disabled WHEN add smoke request THEN smoke is saved without location`() =
        runTest {
            coEvery { userPreferencesRepository.fetch() } returns
                UserPreferences(locationTrackingEnabled = false)
            coEvery { locationCaptureService.locationTrackingAvailability(false) } returns
                LocationTrackingAvailability(
                    preferenceEnabled = false,
                    permissionGranted = false,
                    providerEnabled = false,
                )

            mobile.handleWearRequest(WearPaths.ADD_SMOKE)

            coVerify { smokeRepository.addSmoke(any(), null) }
            coVerify(exactly = 0) { locationCaptureService.captureCurrentLocation() }
        }

    @Test
    fun `GIVEN tracking enabled but provider off WHEN add smoke request THEN smoke is saved without location`() =
        runTest {
            coEvery { userPreferencesRepository.fetch() } returns
                UserPreferences(locationTrackingEnabled = true)
            coEvery { locationCaptureService.locationTrackingAvailability(true) } returns
                LocationTrackingAvailability(
                    preferenceEnabled = true,
                    permissionGranted = true,
                    providerEnabled = false,
                )

            mobile.handleWearRequest(WearPaths.ADD_SMOKE)

            coVerify { smokeRepository.addSmoke(any(), null) }
            coVerify(exactly = 0) { locationCaptureService.captureCurrentLocation() }
        }
}
