package com.feragusper.smokeanalytics.libraries.architecture.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformServicesTest {

    @Test
    fun locationTrackingAvailability_allEnabled_isReady() {
        val availability = LocationTrackingAvailability(
            preferenceEnabled = true,
            permissionGranted = true,
            providerEnabled = true,
        )
        assertTrue(availability.isReady)
    }

    @Test
    fun locationTrackingAvailability_preferenceDisabled_isNotReady() {
        val availability = LocationTrackingAvailability(
            preferenceEnabled = false,
            permissionGranted = true,
            providerEnabled = true,
        )
        assertFalse(availability.isReady)
    }

    @Test
    fun locationTrackingAvailability_permissionDenied_isNotReady() {
        val availability = LocationTrackingAvailability(
            preferenceEnabled = true,
            permissionGranted = false,
            providerEnabled = true,
        )
        assertFalse(availability.isReady)
    }

    @Test
    fun locationTrackingAvailability_providerDisabled_isNotReady() {
        val availability = LocationTrackingAvailability(
            preferenceEnabled = true,
            permissionGranted = true,
            providerEnabled = false,
        )
        assertFalse(availability.isReady)
    }

    @Test
    fun locationTrackingAvailability_allDisabled_isNotReady() {
        val availability = LocationTrackingAvailability(
            preferenceEnabled = false,
            permissionGranted = false,
            providerEnabled = false,
        )
        assertFalse(availability.isReady)
    }

    @Test
    fun coordinate_holdsLatAndLng() {
        val coord = Coordinate(latitude = 40.71, longitude = -74.00)
        assertEquals(40.71, coord.latitude)
        assertEquals(-74.00, coord.longitude)
    }

    @Test
    fun widgetSnapshot_holdsAllFields() {
        val snapshot = WidgetSnapshot(
            todayCount = 5,
            elapsedHours = 2,
            elapsedMinutes = 30,
            targetGapMinutes = 90,
            averageSmokesPerDayWeek = 12.5,
        )
        assertEquals(5, snapshot.todayCount)
        assertEquals(2, snapshot.elapsedHours)
        assertEquals(30, snapshot.elapsedMinutes)
        assertEquals(90, snapshot.targetGapMinutes)
        assertEquals(12.5, snapshot.averageSmokesPerDayWeek)
    }
}

