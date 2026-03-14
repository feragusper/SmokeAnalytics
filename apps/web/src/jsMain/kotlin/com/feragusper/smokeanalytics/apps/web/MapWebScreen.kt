package com.feragusper.smokeanalytics.apps.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonCard
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

private enum class MapPeriod(val label: String, val days: Int) {
    Day("Day", 1),
    Week("Week", 7),
    Month("Month", 31),
}

private data class MapCluster(
    val point: GeoPoint,
    val count: Int,
)

@Composable
fun MapWebScreen(
    fetchSmokesUseCase: FetchSmokesUseCase,
) {
    var period by remember { mutableStateOf(MapPeriod.Week) }
    var clusters by remember { mutableStateOf<List<MapCluster>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(period) {
        loading = true
        val end = Clock.System.now()
        val start = end.minus(period.days, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
        clusters = clusterSmokes(fetchSmokesUseCase(start, end), period)
        loading = false
    }

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "Map",
            eyebrow = "Locations",
            badgeText = "${clusters.sumOf { it.count }} smokes",
            actions = {
                MapPeriod.entries.forEach { candidate ->
                    PrimaryButton(
                        text = candidate.label,
                        onClick = { period = candidate },
                        enabled = !loading && candidate != period,
                    )
                }
            }
        )

        when {
            loading -> LoadingSkeletonCard(heightPx = 320, lineWidths = listOf("50%", "30%"))
            clusters.isEmpty() -> SurfaceCard {
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("No mapped smokes yet") }
                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                    Text("Enable location tracking in Settings and add more smoke entries.")
                }
            }

            else -> {
                SurfaceCard {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Geography") }
                    Div(attrs = {
                        attr(
                            "style",
                            "position:relative;width:100%;height:320px;background:#eaf2ef;border-radius:16px;overflow:hidden;"
                        )
                    }) {
                        clusters.forEach { cluster ->
                            val x = ((cluster.point.longitude + 180.0) / 360.0) * 1000.0
                            val y = ((90.0 - cluster.point.latitude) / 180.0) * 480.0
                            val size = (20 + cluster.count * 4).coerceAtMost(56)
                            Div(attrs = {
                                attr(
                                    "style",
                                    "position:absolute;left:${x - size / 2}px;top:${y - size / 2}px;width:${size}px;height:${size}px;border-radius:999px;background:rgba(0,106,106,0.72);display:flex;align-items:center;justify-content:center;color:white;font-size:12px;font-weight:600;"
                                )
                            }) {
                                Text(cluster.count.toString())
                            }
                        }
                    }
                }

                SurfaceCard {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Clusters") }
                    clusters.forEach { cluster ->
                        Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                            Text("${cluster.count} smokes near ${cluster.point.latitude.asCoord()}, ${cluster.point.longitude.asCoord()}")
                            A(
                                href = "https://www.openstreetmap.org/?mlat=${cluster.point.latitude}&mlon=${cluster.point.longitude}#map=13/${cluster.point.latitude}/${cluster.point.longitude}",
                                attrs = { attr("target", "_blank") }
                            ) {
                                Text("Open in OSM")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun clusterSmokes(
    smokes: List<Smoke>,
    period: MapPeriod,
): List<MapCluster> {
    val precision = when (period) {
        MapPeriod.Day -> 2
        MapPeriod.Week -> 1
        MapPeriod.Month -> 0
    }

    return smokes
        .mapNotNull { it.location }
        .groupingBy { point ->
            val scale = 10.0.pow(precision.toDouble())
            val lat = round(point.latitude * scale) / scale
            val lon = round(point.longitude * scale) / scale
            lat to lon
        }
        .eachCount()
        .map { (key, count) ->
            MapCluster(GeoPoint(key.first, key.second), count)
        }
}

private fun Double.asCoord(): String {
    val scaled = round(this * 1000.0) / 1000.0
    val negative = scaled < 0
    val absValue = abs(scaled)
    val whole = absValue.toInt()
    val fraction = ((absValue - whole) * 1000).toInt().toString().padStart(3, '0')
    return (if (negative) "-" else "") + whole.toString() + "." + fraction
}
