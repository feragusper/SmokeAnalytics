package com.feragusper.smokeanalytics.wear

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import com.feragusper.smokeanalytics.R
import com.feragusper.smokeanalytics.libraries.architecture.presentation.BuildConfig
import com.feragusper.smokeanalytics.tile.TileIntent
import com.feragusper.smokeanalytics.tile.TileViewModel
import com.feragusper.smokeanalytics.tile.TileViewState
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* best-effort */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TileViewModel.initialize(this)

        if (BuildConfig.DEBUG && Timber.forest().isEmpty()) {
            Timber.plant(Timber.DebugTree())
        }

        // Ask once so the tile can geolocate smokes from the watch. Tiles can't request
        // permissions themselves, so we do it from the app.
        requestLocationPermissionIfNeeded()

        setContent {
            WearApp(
                onRefresh = { TileViewModel.intents().trySend(TileIntent.RefreshSmokes) },
                onAddSmoke = { TileViewModel.intents().trySend(TileIntent.AddSmoke(System.currentTimeMillis())) },
            )
        }
    }

    private fun requestLocationPermissionIfNeeded() {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) return
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }
}

@Composable
private fun WearApp(
    onRefresh: () -> Unit,
    onAddSmoke: () -> Unit,
) {
    val state by TileViewModel.states().collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        onRefresh()
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = WearBackground,
        ) {
            WearHomeContent(
                state = state,
                onRefresh = onRefresh,
                onAddSmoke = onAddSmoke,
            )
        }
    }
}

@Composable
private fun WearHomeContent(
    state: TileViewState,
    onRefresh: () -> Unit,
    onAddSmoke: () -> Unit,
) {
    val listState = rememberScalingLazyListState()
    val configuration = LocalConfiguration.current
    val horizontalInset = if (configuration.isScreenRound) 30.dp else 18.dp
    var trackFeedbackNonce by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(WearBackground),
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        },
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(WearBackground),
            contentPadding = PaddingValues(
                start = horizontalInset,
                top = 16.dp,
                end = horizontalInset,
                bottom = 18.dp,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        ) {
            item {
                SmokeCount(
                    todayCount = state.todayCount,
                    trackFeedbackNonce = trackFeedbackNonce,
                )
            }
            item {
                Text(
                    text = stringResourceCompat(R.string.next_pace_short, state.nextSmokeLabel()),
                    modifier = Modifier.fillMaxWidth(),
                    color = WearOnSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
            item {
                Text(
                    text = state.lastSmokeLabel(),
                    modifier = Modifier.fillMaxWidth(),
                    color = WearMuted,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
            item {
                Spacer(modifier = Modifier.height(6.dp))
            }
            item {
                WearActionButtons(
                    state = state,
                    onRefresh = onRefresh,
                    onAddSmoke = {
                        trackFeedbackNonce += 1
                        onAddSmoke()
                    },
                )
            }
        }
    }
}

@Composable
private fun WearActionButtons(
    state: TileViewState,
    onRefresh: () -> Unit,
    onAddSmoke: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = onRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp),
            enabled = !state.refreshRequestInFlight,
            colors = ButtonDefaults.buttonColors(
                containerColor = WearSurface,
                contentColor = WearOnSurface,
                disabledContainerColor = WearSurface,
                disabledContentColor = WearMuted,
            ),
        ) {
            Text(
                text = stringResourceCompat(
                    if (state.refreshRequestInFlight) {
                        R.string.sync_smokes_syncing
                    } else {
                        R.string.sync_smokes
                    },
                ),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
        Button(
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onAddSmoke()
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WearPrimary,
                contentColor = WearPrimaryContent,
            ),
        ) {
            Text(
                text = stringResourceCompat(R.string.add_smoke_track),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SmokeCount(
    todayCount: Int?,
    trackFeedbackNonce: Int,
) {
    val countScale = remember { Animatable(1f) }
    var showTrackFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(trackFeedbackNonce) {
        if (trackFeedbackNonce > 0) {
            showTrackFeedback = true
            countScale.snapTo(1f)
            countScale.animateTo(1.14f, animationSpec = tween(durationMillis = 140))
            countScale.animateTo(1f, animationSpec = tween(durationMillis = 260))
            delay(320)
            showTrackFeedback = false
        }
    }

    Box(
        modifier = Modifier
            .sizeIn(minWidth = 58.dp, minHeight = 58.dp)
            .scale(countScale.value)
            .clip(CircleShape)
            .background(WearSurface)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = showTrackFeedback,
            enter = fadeIn(animationSpec = tween(120)) + scaleIn(initialScale = 0.72f),
            exit = fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 1.18f),
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Text(
                text = "+1",
                color = WearPrimary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        if (todayCount == null) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = WearPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = todayCount.toString(),
                    color = WearPrimary,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun stringResourceCompat(
    id: Int,
    vararg formatArgs: Any,
): String = androidx.compose.ui.res.stringResource(id = id, formatArgs = formatArgs)

@Composable
private fun TileViewState.lastSmokeLabel(): String {
    val timestamp = lastSmokeTimestamp
    if (timestamp == null || timestamp == 0L) {
        return androidx.compose.ui.res.stringResource(R.string.last_smoke_na)
    }

    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> androidx.compose.ui.res.stringResource(R.string.last_smoke_just_now)
        diff < TimeUnit.HOURS.toMillis(1) -> androidx.compose.ui.res.stringResource(
            R.string.last_smoke_minutes_ago,
            TimeUnit.MILLISECONDS.toMinutes(diff),
        )
        diff < TimeUnit.DAYS.toMillis(1) -> androidx.compose.ui.res.stringResource(
            R.string.last_smoke_hours_ago,
            TimeUnit.MILLISECONDS.toHours(diff),
        )
        else -> androidx.compose.ui.res.stringResource(
            R.string.last_smoke_days_ago,
            TimeUnit.MILLISECONDS.toDays(diff),
        )
    }
}

@Composable
private fun TileViewState.nextSmokeLabel(): String {
    val targetMinutes = targetGapMinutes
    val timestamp = lastSmokeTimestamp
    if (targetMinutes == null || targetMinutes <= 0 || timestamp == null || timestamp == 0L) {
        return androidx.compose.ui.res.stringResource(R.string.pace_time_na)
    }

    val elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - timestamp)
    val remainingMinutes = (targetMinutes.toLong() - elapsedMinutes).coerceAtLeast(0L)

    return if (remainingMinutes == 0L) {
        androidx.compose.ui.res.stringResource(R.string.pace_ready_now)
    } else {
        remainingMinutes.toDurationLabel()
    }
}

private fun Long.toDurationLabel(): String {
    val hours = this / 60
    val minutes = this % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

private val WearBackground = Color.Black
private val WearSurface = Color(0xFF10241F)
private val WearPrimary = Color(0xFF80F2D7)
private val WearPrimaryContent = Color(0xFF05201A)
private val WearOnSurface = Color(0xFFF5FAF8)
private val WearMuted = Color(0xFF9FB6AF)
