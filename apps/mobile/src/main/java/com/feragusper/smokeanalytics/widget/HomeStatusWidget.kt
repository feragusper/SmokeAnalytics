package com.feragusper.smokeanalytics.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
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
        provideContent {
            GlanceTheme {
                WidgetContent(snapshot = snapshot)
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
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFFF8FBFA)))
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start,
    ) {
        Text(
            text = "${snapshot.todayCount} today",
            style = TextStyle(
                color = ColorProvider(Color(0xFF101414)),
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = "Last ${snapshot.elapsedHours}h ${snapshot.elapsedMinutes}m",
            style = TextStyle(color = ColorProvider(Color(0xFF466160))),
        )

        Spacer(GlanceModifier.height(12.dp))

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Metric("Week", snapshot.weekCount.toString())
            Spacer(GlanceModifier.width(16.dp))
            Metric("Month", snapshot.monthCount.toString())
        }

        Spacer(GlanceModifier.height(8.dp))

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Metric("Streak", "${snapshot.currentStreakHours}h")
            Spacer(GlanceModifier.width(16.dp))
            Metric("Spent", formatMoney(snapshot.spentToday))
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

private fun formatMoney(value: Double): String = "€" + String.format("%.2f", value)
