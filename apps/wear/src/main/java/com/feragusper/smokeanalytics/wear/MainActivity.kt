package com.feragusper.smokeanalytics.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feragusper.smokeanalytics.R
import com.feragusper.smokeanalytics.libraries.architecture.presentation.BuildConfig
import com.feragusper.smokeanalytics.tile.TileIntent
import com.feragusper.smokeanalytics.tile.TileViewModel
import com.feragusper.smokeanalytics.tile.TileViewState
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TileViewModel.initialize(this)

        if (BuildConfig.DEBUG && Timber.forest().isEmpty()) {
            Timber.plant(Timber.DebugTree())
        }

        setContent {
            WearApp(
                onRefresh = { TileViewModel.intents().trySend(TileIntent.RefreshSmokes) },
                onAddSmoke = { TileViewModel.intents().trySend(TileIntent.AddSmoke(System.currentTimeMillis())) },
            )
        }
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WearBackground)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            SmokeCount(state.todayCount)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResourceCompat(R.string.next_pace_short, state.nextSmokeLabel()),
                color = WearOnSurface,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = state.lastSmokeLabel(),
                color = WearMuted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onRefresh,
                    modifier = Modifier.weight(1f),
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
                        maxLines = 1,
                    )
                }
                Button(
                    onClick = onAddSmoke,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WearPrimary,
                        contentColor = WearPrimaryContent,
                    ),
                ) {
                    Text(
                        text = stringResourceCompat(R.string.add_smoke_track),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun SmokeCount(todayCount: Int?) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(WearSurface),
        contentAlignment = Alignment.Center,
    ) {
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
                    maxLines = 1,
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

private val WearBackground = Color(0xFF071311)
private val WearSurface = Color(0xFF10241F)
private val WearPrimary = Color(0xFF80F2D7)
private val WearPrimaryContent = Color(0xFF05201A)
private val WearOnSurface = Color(0xFFF5FAF8)
private val WearMuted = Color(0xFF9FB6AF)
