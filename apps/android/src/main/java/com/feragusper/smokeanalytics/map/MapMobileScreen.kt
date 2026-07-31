package com.feragusper.smokeanalytics.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.feragusper.smokeanalytics.R
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feragusper.smokeanalytics.BuildConfig
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeMapCluster
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeMapPeriod
import kotlinx.datetime.LocalDate
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
    refreshNonce: Int = 0,
    embedded: Boolean = false,
    period: SmokeMapPeriod = SmokeMapPeriod.Week,
    selectedDate: LocalDate? = null,
    viewModel: MapMobileViewModel = koinViewModel(),
) {
    LaunchedEffect(refreshNonce) {
        if (refreshNonce > 0) viewModel.onScreenVisible()
    }
    // The analytics shell owns the period + date via its shared tabs/navigator.
    LaunchedEffect(period, selectedDate) {
        if (selectedDate != null) viewModel.onWindowChange(period, selectedDate)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    MapMobileScreen(
        modifier = modifier,
        state = state,
        embedded = embedded,
        onSelectCluster = viewModel::onSelectCluster,
        onRetry = viewModel::refresh,
    )
}

@Composable
private fun MapMobileScreen(
    modifier: Modifier = Modifier,
    state: MapMobileState,
    embedded: Boolean,
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
            embedded = embedded,
            onSelectCluster = onSelectCluster,
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MapSkeletonBlock(modifier = Modifier.fillMaxWidth().height(360.dp))
        MapSkeletonBlock(modifier = Modifier.fillMaxWidth().height(120.dp))
    }
}

/** Gently pulsing placeholder shown while clusters load (no shimmer dependency needed). */
@Composable
private fun MapSkeletonBlock(modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "mapSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)),
    )
}

@Composable
private fun LoadedState(
    modifier: Modifier,
    state: MapMobileState,
    embedded: Boolean,
    onSelectCluster: (SmokeMapCluster) -> Unit,
) {
    val activeCluster = state.selectedCluster ?: state.clusters.first()
    val areaName = rememberAreaName(activeCluster)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
                        text = clusterRankLabel(state.clusters.indexOf(activeCluster).coerceAtLeast(0)),
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
                        text = stringResource(
                            R.string.map_area_grouping,
                            activeCluster.count,
                            activeCluster.radiusMeters,
                        ),
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

        itemsIndexed(state.clusters) { index, cluster ->
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
                            text = clusterRankLabel(index),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = stringResource(R.string.map_smokes_in_area, cluster.count),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = if (cluster == activeCluster) stringResource(R.string.map_viewing) else stringResource(R.string.map_view),
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
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOff,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.map_location_off),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.map_location_off_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.map_rest_works),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Localized rank label for a cluster by its position (0 = most frequent). */
@Composable
private fun clusterRankLabel(rank: Int): String = when (rank) {
    0 -> stringResource(R.string.map_top_area)
    1 -> stringResource(R.string.map_second_area)
    2 -> stringResource(R.string.map_third_area)
    else -> stringResource(R.string.map_area_n, rank + 1)
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Map,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.map_no_mapped),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.map_no_mapped_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.map_once_enough),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
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
                StatusChip(text = stringResource(R.string.map_needs_attention))
                Text(
                    text = stringResource(R.string.map_view_unavailable),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = stringResource(R.string.map_could_not_refresh),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.map_this_does_not_affect),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.map_retry))
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
            .height(300.dp),
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
