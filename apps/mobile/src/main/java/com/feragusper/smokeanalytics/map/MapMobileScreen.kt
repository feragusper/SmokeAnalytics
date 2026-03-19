package com.feragusper.smokeanalytics.map

import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeMapCluster
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeMapPeriod

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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = activeCluster.label,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "${activeCluster.count} smokes grouped in an approximate ${activeCluster.radiusMeters} m area.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    AreaPreview(cluster = activeCluster)
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
private fun AreaPreview(cluster: SmokeMapCluster) {
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        colors = CardDefaults.cardColors(
            containerColor = surface,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension * 0.32f
                drawCircle(
                    color = primary.copy(alpha = 0.14f),
                    radius = radius,
                    center = center,
                )
                drawCircle(
                    color = primary.copy(alpha = 0.55f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                )
                drawCircle(
                    color = primary,
                    radius = 8.dp.toPx(),
                    center = center,
                )
            }

            Column(
                modifier = Modifier.align(Alignment.TopStart),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Approximate area",
                    style = MaterialTheme.typography.labelLarge,
                    color = onSurfaceVariant,
                )
                Text(
                    text = "${cluster.radiusMeters} m radius",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Center ${cluster.point.latitude.roundedCoordinate()} / ${cluster.point.longitude.roundedCoordinate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant,
                )
            }

            Text(
                modifier = Modifier.align(Alignment.BottomStart),
                text = "This preview stays in-app and avoids a billed map SDK.",
                style = MaterialTheme.typography.bodySmall,
                color = onSurfaceVariant,
            )
        }
    }
}

private fun Double.roundedCoordinate(): String = String.format("%.4f", this)

private fun UserPreferences?.orDefault(): UserPreferences = this ?: UserPreferences()
