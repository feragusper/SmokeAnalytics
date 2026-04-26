package com.feragusper.smokeanalytics.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.feragusper.smokeanalytics.MainActivity
import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetSnapshot
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

class HomeStatusWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = WidgetSnapshotStore.readFreshOrStored(context)
        val openAppIntent = Intent(context, MainActivity::class.java)
        val quickAddIntent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_WIDGET_QUICK_ADD
        }
        provideContent {
            GlanceTheme {
                WidgetContent(
                    snapshot = snapshot,
                    openAppIntent = openAppIntent,
                    quickAddIntent = quickAddIntent,
                )
            }
        }
    }
}

class HomeStatusWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HomeStatusWidget()
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface HomeStatusWidgetEntryPoint {
    fun fetchSmokeCountListUseCase(): FetchSmokeCountListUseCase
    fun fetchUserPreferencesUseCase(): FetchUserPreferencesUseCase
}

@Composable
private fun WidgetContent(
    snapshot: WidgetSnapshot,
    openAppIntent: Intent,
    quickAddIntent: Intent,
) {
    val widgetSize = LocalSize.current
    val compact = widgetSize.height < 150.dp || widgetSize.width < 220.dp
    val elapsedMinutes = snapshot.elapsedHours * 60L + snapshot.elapsedMinutes
    val remainingMinutes = snapshot.remainingMinutesUntilNextSmoke()
    val progressFraction = snapshot.progressFraction()
    val status = snapshot.gapStatus()

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.Background)
            .cornerRadius(28.dp)
            .padding(if (compact) 10.dp else 14.dp)
            .clickable(actionStartActivity(openAppIntent)),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start,
    ) {
        WidgetHeader(
            compact = compact,
            quickAddIntent = quickAddIntent,
        )
        Spacer(GlanceModifier.height(if (compact) 6.dp else 10.dp))
        GapHeroCard(
            remainingLabel = remainingMinutes.toNextSmokeLabel(),
            status = status,
            targetLabel = snapshot.targetGapMinutes.toGapLabel(),
            progressFraction = progressFraction,
            compact = compact,
        )
        Spacer(GlanceModifier.height(if (compact) 6.dp else 10.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        ) {
            MetricCard(
                marker = "TOD",
                label = "Today",
                value = snapshot.todayCount.toString(),
                modifier = GlanceModifier.defaultWeight(),
            )
            Spacer(GlanceModifier.width(6.dp))
            MetricCard(
                marker = "AVG",
                label = "7d avg",
                value = snapshot.averageSmokesPerDayWeek.formatOneDecimal(),
                modifier = GlanceModifier.defaultWeight(),
            )
            if (!compact) {
                Spacer(GlanceModifier.width(6.dp))
                MetricCard(
                    marker = "SIN",
                    label = "Since last",
                    value = elapsedMinutes.toDurationLabel(),
                    modifier = GlanceModifier.defaultWeight(),
                )
            }
        }
    }
}

@Composable
private fun WidgetHeader(
    compact: Boolean,
    quickAddIntent: Intent,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        Column(
            modifier = GlanceModifier.defaultWeight(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.Start,
        ) {
            Text(
                text = "Goal focus",
                style = TextStyle(
                    color = WidgetColors.Text,
                    fontSize = if (compact) 13.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
            )
            if (!compact) {
                Text(
                    text = "Home snapshot",
                    style = TextStyle(
                        color = WidgetColors.Muted,
                        fontSize = 11.sp,
                    ),
                    maxLines = 1,
                )
            }
        }
        QuickAddChip(
            label = "+ Track",
            intent = quickAddIntent,
        )
    }
}

@Composable
private fun GapHeroCard(
    remainingLabel: String,
    status: GapStatus,
    targetLabel: String,
    progressFraction: Float,
    compact: Boolean,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(WidgetColors.Card)
            .cornerRadius(22.dp)
            .padding(
                horizontal = if (compact) 10.dp else 14.dp,
                vertical = if (compact) 8.dp else 12.dp,
            ),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.Start,
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        ) {
            StatusMarker(status)
            Spacer(GlanceModifier.width(8.dp))
            Column(
                modifier = GlanceModifier.defaultWeight(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.Start,
            ) {
                Text(
                    text = remainingLabel,
                    style = TextStyle(
                        color = WidgetColors.Text,
                        fontSize = if (compact) 20.sp else 26.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    maxLines = 1,
                )
                Text(
                    text = status.message(targetLabel),
                    style = TextStyle(
                        color = WidgetColors.Muted,
                        fontSize = if (compact) 11.sp else 12.sp,
                    ),
                    maxLines = 1,
                )
            }
        }
        if (!compact) {
            Spacer(GlanceModifier.height(10.dp))
            ProgressDots(progressFraction = progressFraction)
        }
    }
}

@Composable
private fun StatusMarker(status: GapStatus) {
    Text(
        text = status.marker,
        modifier = GlanceModifier
            .background(status.container)
            .cornerRadius(14.dp)
            .padding(horizontal = 7.dp, vertical = 5.dp),
        style = TextStyle(
            color = status.content,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        ),
        maxLines = 1,
    )
}

@Composable
private fun ProgressDots(progressFraction: Float) {
    val activeDots = (progressFraction * ProgressDotCount).toInt().coerceIn(1, ProgressDotCount)
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        repeat(ProgressDotCount) { index ->
            ProgressDot(
                active = index < activeDots,
                modifier = GlanceModifier.defaultWeight(),
            )
            if (index != ProgressDotCount - 1) {
                Spacer(GlanceModifier.width(5.dp))
            }
        }
    }
}

@Composable
private fun ProgressDot(
    active: Boolean,
    modifier: GlanceModifier,
) {
    Spacer(
        modifier = modifier
            .height(6.dp)
            .background(if (active) WidgetColors.Primary else WidgetColors.ProgressTrack)
            .cornerRadius(999.dp),
    )
}

@Composable
private fun MetricCard(
    marker: String,
    label: String,
    value: String,
    modifier: GlanceModifier = GlanceModifier,
) {
    Column(
        modifier = modifier
            .background(WidgetColors.CardMuted)
            .cornerRadius(18.dp)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.Start,
    ) {
        Text(
            text = marker,
            style = TextStyle(
                color = WidgetColors.Primary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
            ),
            maxLines = 1,
        )
        Text(
            text = value,
            style = TextStyle(
                color = WidgetColors.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            ),
            maxLines = 1,
        )
        Text(
            text = label,
            style = TextStyle(
                color = WidgetColors.Muted,
                fontSize = 10.sp,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun QuickAddChip(
    label: String,
    intent: Intent,
) {
    Text(
        text = label,
        modifier = GlanceModifier
            .background(WidgetColors.Primary)
            .cornerRadius(999.dp)
            .clickable(actionStartActivity(intent))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        style = TextStyle(
            color = WidgetColors.OnPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        ),
        maxLines = 1,
    )
}

private fun Int.toGapLabel(): String {
    val hours = this / 60
    val minutes = this % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
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

private fun Long.toNextSmokeLabel(): String =
    if (this <= 0L) "Ready now" else "${toDurationLabel()} left"

private fun WidgetSnapshot.remainingMinutesUntilNextSmoke(): Long {
    val target = targetGapMinutes.takeIf { it > 0 } ?: return 0L
    val elapsed = elapsedHours * 60L + elapsedMinutes
    return (target - elapsed).coerceAtLeast(0L)
}

private fun WidgetSnapshot.progressFraction(): Float {
    val target = targetGapMinutes.takeIf { it > 0 } ?: return 1f
    val elapsed = elapsedHours * 60L + elapsedMinutes
    return (elapsed.toFloat() / target.toFloat()).coerceIn(0f, 1f)
}

private fun WidgetSnapshot.gapStatus(): GapStatus {
    val target = targetGapMinutes
    val elapsed = elapsedHours * 60L + elapsedMinutes
    return when {
        target <= 0 -> GapStatus.Steady
        elapsed >= target -> GapStatus.Ready
        progressFraction() >= 0.66f -> GapStatus.Near
        else -> GapStatus.Building
    }
}

private fun Double.formatOneDecimal(): String = String.format("%.1f", this)

private enum class GapStatus(
    val marker: String,
    val container: ColorProvider,
    val content: ColorProvider,
) {
    Ready(
        marker = "OK",
        container = ColorProvider(Color(0xFFD9F1E3)),
        content = ColorProvider(Color(0xFF0E5B35)),
    ),
    Near(
        marker = "GAP",
        container = ColorProvider(Color(0xFFFFE8C2)),
        content = ColorProvider(Color(0xFF7A4A00)),
    ),
    Building(
        marker = "GAP",
        container = ColorProvider(Color(0xFFFFDAD6)),
        content = ColorProvider(Color(0xFF8C1D18)),
    ),
    Steady(
        marker = "NOW",
        container = ColorProvider(Color(0xFFE0EEF0)),
        content = ColorProvider(Color(0xFF234F55)),
    );

    fun message(targetLabel: String): String = when (this) {
        Ready -> "Next smoke is past target $targetLabel"
        Near -> "Almost at target $targetLabel"
        Building -> "Wait until target $targetLabel"
        Steady -> "Live countdown from latest data"
    }
}

private const val ProgressDotCount = 5

private object WidgetColors {
    val Background = ColorProvider(Color(0xFFF5FAF8))
    val Card = ColorProvider(Color(0xFFFFFFFF))
    val CardMuted = ColorProvider(Color(0xFFEAF3F0))
    val Text = ColorProvider(Color(0xFF101414))
    val Muted = ColorProvider(Color(0xFF506866))
    val Primary = ColorProvider(Color(0xFF126C5A))
    val OnPrimary = ColorProvider(Color(0xFFFFFFFF))
    val ProgressTrack = ColorProvider(Color(0xFFD2E2DE))
}
