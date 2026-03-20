package com.feragusper.smokeanalytics.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feragusper.smokeanalytics.BuildConfig
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeMapCluster
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeMapPeriod
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Composable
fun MapMobileRoute(
    modifier: Modifier = Modifier,
    viewModel: MapMobileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MapMobileScreen(
        modifier = modifier,
        state = state,
        onPeriodChange = viewModel::onPeriodChange,
        onSelectCluster = viewModel::onSelectCluster,
        onRetry = viewModel::refresh,
    )
}

@Composable
private fun MapMobileScreen(
    modifier: Modifier = Modifier,
    state: MapMobileState,
    onPeriodChange: (SmokeMapPeriod) -> Unit,
    onSelectCluster: (SmokeMapCluster) -> Unit,
    onRetry: () -> Unit,
) {
    when {
        state.isLoading -> LoadingState(modifier = modifier)
        state.error -> ErrorState(modifier = modifier, onRetry = onRetry)
        !state.preferences.orDefault().locationTrackingEnabled -> DisabledState(modifier = modifier)
        state.clusters.isEmpty() -> EmptyState(modifier = modifier)
        else -> LoadedState(
            modifier = modifier,
            state = state,
            onPeriodChange = onPeriodChange,
            onSelectCluster = onSelectCluster,
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun LoadedState(
    modifier: Modifier,
    state: MapMobileState,
    onPeriodChange: (SmokeMapPeriod) -> Unit,
    onSelectCluster: (SmokeMapCluster) -> Unit,
) {
    val activeCluster = state.selectedCluster ?: state.clusters.first()
    val areaName = rememberAreaName(activeCluster)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Map",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "Approximate smoking areas based on tracked locations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmokeMapPeriod.entries.forEach { period ->
                        AssistChip(
                            onClick = { onPeriodChange(period) },
                            label = { Text(period.name) },
                            colors = if (period == state.period) {
                                AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            } else {
                                AssistChipDefaults.assistChipColors()
                            }
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = activeCluster.label,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    areaName?.let { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "${activeCluster.count} smokes grouped in an approximate ${activeCluster.radiusMeters} m area.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (BuildConfig.GOOGLE_MAPS_API_KEY.isBlank()) {
                        Text(
                            text = "Google Maps key missing for this build. Add google.maps.android.api.key.staging or google.maps.android.api.key.production to local.properties.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        GoogleMapPreview(cluster = activeCluster)
                    }
                }
            }
        }

        item {
            Text(
                text = "Areas",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        items(state.clusters) { cluster ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (cluster == activeCluster) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    }
                ),
                onClick = { onSelectCluster(cluster) },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = cluster.label,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = "${cluster.count} smokes in this area",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = if (cluster == activeCluster) "Viewing" else "View",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (cluster == activeCluster) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DisabledState(modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Location tracking is off",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enable location tracking in Settings to unlock map insights.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "No mapped smokes yet",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Track a few smokes with location enabled to start seeing areas here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorState(
    modifier: Modifier,
    onRetry: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Map could not be loaded",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try refreshing in a moment.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun GoogleMapPreview(cluster: SmokeMapCluster) {
    val mapView = rememberMapView()
    AndroidView(
        factory = { mapView },
        update = {
            it.getMapAsync { googleMap ->
                googleMap.applyCluster(cluster)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
    )
}

@Composable
private fun rememberMapView(): MapView {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
        }
    }

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    return mapView
}

private fun GoogleMap.applyCluster(cluster: SmokeMapCluster) {
    val center = LatLng(cluster.point.latitude, cluster.point.longitude)
    clear()
    uiSettings.apply {
        isMapToolbarEnabled = false
        isMyLocationButtonEnabled = false
        isZoomControlsEnabled = false
        isCompassEnabled = false
    }
    moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoomForRadius(cluster.radiusMeters)))
    addCircle(
        CircleOptions()
            .center(center)
            .radius(cluster.radiusMeters.toDouble())
            .strokeWidth(3f)
            .strokeColor(0xFF006A6A.toInt())
            .fillColor(0x336AD2D8)
    )
}

private fun zoomForRadius(radiusMeters: Int): Float = when {
    radiusMeters <= 150 -> 15.5f
    radiusMeters <= 400 -> 14.2f
    radiusMeters <= 1000 -> 12.7f
    else -> 11.5f
}

@Composable
private fun rememberAreaName(cluster: SmokeMapCluster): String? {
    val context = LocalContext.current
    val areaName by produceState<String?>(initialValue = null, cluster.point.latitude, cluster.point.longitude) {
        value = resolveAreaName(
            context = context,
            latitude = cluster.point.latitude,
            longitude = cluster.point.longitude,
        )
    }
    return areaName
}

private suspend fun resolveAreaName(
    context: Context,
    latitude: Double,
    longitude: Double,
): String? {
    val address = reverseGeocode(context, latitude, longitude) ?: return null
    val parts = listOfNotNull(
        address.subLocality,
        address.locality,
        address.adminArea,
    ).distinct()
    return parts.takeIf { it.isNotEmpty() }?.joinToString(", ")
        ?: address.featureName
        ?: address.thoroughfare
}

private suspend fun reverseGeocode(
    context: Context,
    latitude: Double,
    longitude: Double,
): Address? {
    if (!Geocoder.isPresent()) return null
    val geocoder = Geocoder(context, Locale.getDefault())
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocation(
                latitude,
                longitude,
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        continuation.resume(addresses.firstOrNull())
                    }

                    override fun onError(errorMessage: String?) {
                        continuation.resume(null)
                    }
                },
            )
        }
    } else {
        withContext(Dispatchers.IO) {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
        }
    }
}

private fun UserPreferences?.orDefault(): UserPreferences = this ?: UserPreferences()
