package com.feragusper.smokeanalytics.widget

import android.content.Intent
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
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

class HomeStatusWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = WidgetSnapshotStore.read(context)
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

@Composable
private fun WidgetContent(
    snapshot: com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetSnapshot,
    openAppIntent: Intent,
    quickAddIntent: Intent,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFFF8FBFA)))
            .padding(16.dp)
            .clickable(actionStartActivity(openAppIntent)),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start,
    ) {
        Text(
            text = "The Pulse",
            style = TextStyle(
                color = ColorProvider(Color(0xFF101414)),
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = snapshot.todayCount.toString(),
            style = TextStyle(
                color = ColorProvider(Color(0xFF101414)),
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = "Cigarettes today",
            style = TextStyle(color = ColorProvider(Color(0xFF466160))),
        )
        Spacer(GlanceModifier.height(8.dp))
        Text(
            text = "Last smoke ${snapshot.elapsedHours}h ${snapshot.elapsedMinutes}m ago",
            style = TextStyle(color = ColorProvider(Color(0xFF466160))),
        )
        Text(
            text = "Target gap ${snapshot.targetGapMinutes.toGapLabel()}",
            style = TextStyle(color = ColorProvider(Color(0xFF466160))),
        )

        Spacer(GlanceModifier.height(12.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.Start,
        ) {
            Metric("7d avg", snapshot.averageSmokesPerDayWeek.formatOneDecimal())
            Spacer(GlanceModifier.width(16.dp))
            ActionChip(
                label = "Quick add",
                intent = quickAddIntent,
            )
        }
    }
}

@Composable
private fun Metric(
    label: String,
    value: String,
) {
    Column {
        Text(
            text = label,
            style = TextStyle(color = ColorProvider(Color(0xFF466160))),
        )
        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(Color(0xFF101414)),
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun ActionChip(
    label: String,
    intent: Intent,
) {
    Text(
        text = label,
        modifier = GlanceModifier
            .background(ColorProvider(Color(0xFF101414)))
            .clickable(actionStartActivity(intent))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        style = TextStyle(
            color = ColorProvider(Color(0xFFF8FBFA)),
            fontWeight = FontWeight.Bold,
        ),
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

private fun Double.formatOneDecimal(): String = String.format("%.1f", this)
