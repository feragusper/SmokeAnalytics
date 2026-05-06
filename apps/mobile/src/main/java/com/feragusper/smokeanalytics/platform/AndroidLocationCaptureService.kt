package com.feragusper.smokeanalytics.platform

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.feragusper.smokeanalytics.libraries.architecture.domain.Coordinate
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationTrackingAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidLocationCaptureService @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationCaptureService {

    override suspend fun locationTrackingAvailability(preferenceEnabled: Boolean): LocationTrackingAvailability {
        val permissionGranted = hasLocationPermission()
        return LocationTrackingAvailability(
            preferenceEnabled = preferenceEnabled,
            permissionGranted = permissionGranted,
            providerEnabled = locationManager()?.let(::hasEnabledLocationProvider) == true,
        )
    }

    @SuppressLint("MissingPermission")
    override suspend fun captureCurrentLocation(): Coordinate? {
        if (!hasLocationPermission()) return null

        val locationManager = locationManager() ?: return null

        val bestLocation = providers
            .filter(locationManager::isProviderEnabled)
            .mapNotNull { provider ->
                runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
            }
            .maxByOrNull { it.time }

        return bestLocation?.let {
            Coordinate(
                latitude = it.latitude,
                longitude = it.longitude,
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    private fun locationManager(): LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private fun hasEnabledLocationProvider(locationManager: LocationManager): Boolean =
        providers.any { provider ->
            runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
        }

    private companion object {
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
    }
}
