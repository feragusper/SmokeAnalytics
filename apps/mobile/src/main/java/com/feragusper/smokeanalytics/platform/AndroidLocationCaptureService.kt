package com.feragusper.smokeanalytics.platform

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.feragusper.smokeanalytics.libraries.architecture.domain.Coordinate
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationTrackingAvailability
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class AndroidLocationCaptureService constructor(
    private val context: Context,
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
        val enabledProviders = providers.filter { provider ->
            runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
        }
        if (enabledProviders.isEmpty()) return null

        // Actively request a fresh fix (API 30+). getLastKnownLocation alone is frequently
        // null or stale — that's why so few smokes ended up geolocated. Fall back to the
        // last known fix when a fresh one isn't available (older OS, or background where a
        // live fix is blocked without ACCESS_BACKGROUND_LOCATION).
        val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestFreshLocation(locationManager, enabledProviders) ?: bestLastKnown(locationManager, enabledProviders)
        } else {
            bestLastKnown(locationManager, enabledProviders)
        }

        return location?.let { Coordinate(latitude = it.latitude, longitude = it.longitude) }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun requestFreshLocation(
        locationManager: LocationManager,
        enabledProviders: List<String>,
    ): Location? {
        // Prefer GPS, then network, then whatever else is on.
        val provider = providers.firstOrNull { it in enabledProviders } ?: return null
        return withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { continuation ->
                val cancellationSignal = CancellationSignal()
                continuation.invokeOnCancellation { cancellationSignal.cancel() }
                runCatching {
                    locationManager.getCurrentLocation(
                        provider,
                        cancellationSignal,
                        context.mainExecutor,
                    ) { location ->
                        if (continuation.isActive) continuation.resume(location)
                    }
                }.onFailure {
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun bestLastKnown(
        locationManager: LocationManager,
        enabledProviders: List<String>,
    ): Location? = enabledProviders
        .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull { it.time }

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
        const val LOCATION_TIMEOUT_MILLIS = 8_000L
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
    }
}
