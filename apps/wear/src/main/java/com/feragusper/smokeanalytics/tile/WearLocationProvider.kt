package com.feragusper.smokeanalytics.tile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Captures the watch's own location for a smoke added from the tile. Smokes added from the
 * watch reach the phone in the background, where the phone can't get a fix — so the watch
 * (Pixel Watch 2 and other GPS watches) captures here and ships the coordinates in the message.
 *
 * Best-effort: returns null when permission is missing, no provider is on, or no fix arrives.
 */
class WearLocationProvider(
    private val context: Context,
) {

    @SuppressLint("MissingPermission")
    suspend fun currentLatLng(): Pair<Double, Double>? {
        if (!hasLocationPermission()) return null
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val enabledProviders = PROVIDERS.filter { provider ->
            runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
        }
        if (enabledProviders.isEmpty()) return null

        val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestFreshLocation(locationManager, enabledProviders) ?: bestLastKnown(locationManager, enabledProviders)
        } else {
            bestLastKnown(locationManager, enabledProviders)
        }
        return location?.let { it.latitude to it.longitude }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun requestFreshLocation(
        locationManager: LocationManager,
        enabledProviders: List<String>,
    ): Location? {
        val provider = PROVIDERS.firstOrNull { it in enabledProviders } ?: return null
        return withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { continuation ->
                val cancellationSignal = CancellationSignal()
                continuation.invokeOnCancellation { cancellationSignal.cancel() }
                runCatching {
                    locationManager.getCurrentLocation(
                        provider,
                        cancellationSignal,
                        context.mainExecutor,
                    ) { location -> if (continuation.isActive) continuation.resume(location) }
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

    private companion object {
        const val LOCATION_TIMEOUT_MILLIS = 5_000L
        val PROVIDERS = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
    }
}
