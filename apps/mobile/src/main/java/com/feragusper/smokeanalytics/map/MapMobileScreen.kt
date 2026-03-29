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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatusChip(text = "Refreshing")
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                Text(
                    text = "Loading geographic clusters",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Pulling repeated smoking areas for the selected period and rebuilding the geographic side of Analytics.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Locations",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Geographic clusters",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            text = "See where repeated smoking clusters show up across the selected period.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(28.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    Text(
                        text = "Most frequent area for the selected period.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
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
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Top clusters",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    state.clusters.take(3).forEachIndexed { index, cluster ->
                        ClusterLegendRow(
                            index = index,
                            cluster = cluster,
                            isActive = cluster == activeCluster,
                            onSelectCluster = onSelectCluster,
                        )
                        if (index < state.clusters.take(3).lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                ),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Observation",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                            text = "Repeated clusters usually point to routines worth protecting or interrupting, especially around commute, breaks, or end-of-day transitions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                }
            }
        }

        item {
            Text(
                text = "All clusters",
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
                shape = RoundedCornerShape(22.dp),
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
private fun ClusterLegendRow(
    index: Int,
    cluster: SmokeMapCluster,
    isActive: Boolean,
    onSelectCluster: (SmokeMapCluster) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                shape = RoundedCornerShape(999.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (index) {
                        0 -> MaterialTheme.colorScheme.primary
                        1 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    }
                ),
            ) {
                Spacer(modifier = Modifier.size(12.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = cluster.label, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${cluster.count} events",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        TextButton(onClick = { onSelectCluster(cluster) }) {
            Text(if (isActive) "Viewing" else "View")
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
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatusChip(text = "Action required")
                Text(
                    text = "Location tracking is off",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "Enable location tracking in You to unlock repeated-area detection, cluster insights, and the geographic side of Analytics.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "The rest of the product still works, but this destination needs location-linked history to become useful.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatusChip(text = "Quiet map")
                Text(
                    text = "No mapped areas yet",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "There is not enough location-linked history for this period yet. Track a few smokes with location enabled to start building repeated areas here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Once enough entries accumulate, the app will cluster repeated areas automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatusChip(text = "Needs attention")
                Text(
                    text = "Map view unavailable",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "The geographic clusters could not be refreshed right now. Keep the selected period and try again in a moment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "This does not affect the rest of your tracked history or analytics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
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
            .height(176.dp),
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
