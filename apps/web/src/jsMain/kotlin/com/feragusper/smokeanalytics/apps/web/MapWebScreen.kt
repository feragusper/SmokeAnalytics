package com.feragusper.smokeanalytics.apps.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonCard
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Iframe
import org.jetbrains.compose.web.dom.Text
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
    val label: String,
)

@Composable
fun MapWebScreen(
    fetchSmokesUseCase: FetchSmokesUseCase,
) {
    var period by remember { mutableStateOf(MapPeriod.Week) }
    var clusters by remember { mutableStateOf<List<MapCluster>>(emptyList()) }
    var selectedCluster by remember { mutableStateOf<MapCluster?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(period) {
        loading = true
        val end = Clock.System.now()
        val start = end.minus(period.days, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
        val rawClusters = clusterSmokes(fetchSmokesUseCase(start, end), period)
        clusters = rawClusters.map { cluster ->
            cluster.copy(label = reverseGeocode(cluster.point))
        }
        selectedCluster = clusters.maxByOrNull { it.count }
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
                val activeCluster = selectedCluster ?: clusters.first()

                SurfaceCard {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(activeCluster.label) }
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                        Text("${activeCluster.count} smokes in this area")
                    }
                    Iframe(attrs = {
                        attr("src", googleEmbedUrl(activeCluster.point))
                        attr("loading", "lazy")
                        attr("referrerpolicy", "no-referrer-when-downgrade")
                        attr("style", "width:100%;height:360px;border:0;border-radius:16px;margin-top:12px;background:#f5f8f8;")
                    })
                    Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                        PrimaryButton(
                            text = "Open in Google Maps",
                            onClick = { window.open(googleMapsUrl(activeCluster.point), "_blank") },
                        )
                        GhostButton(
                            text = "Open in OpenStreetMap",
                            onClick = { window.open(osmUrl(activeCluster.point), "_blank") },
                        )
                    }
                }

                SurfaceCard {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Clusters") }
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                        Text("Choose an area to inspect. Labels use reverse geocoding instead of raw coordinates.")
                    }
                    clusters.forEach { cluster ->
                        Div(
                            attrs = {
                                classes(SmokeWebStyles.listRow)
                                onClick { selectedCluster = cluster }
                                attr("style", "cursor:pointer;margin-top:10px;")
                            }
                        ) {
                            Div {
                                Div(attrs = { classes(SmokeWebStyles.timeText) }) { Text(cluster.label) }
                                Div(attrs = { classes(SmokeWebStyles.subText) }) {
                                    Text("${cluster.count} smokes in this cluster")
                                }
                            }
                            GhostButton(
                                text = if (cluster == activeCluster) "Viewing" else "View",
                                onClick = { selectedCluster = cluster },
                                enabled = cluster != activeCluster,
                            )
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
        .entries
        .sortedByDescending { it.value }
        .map { (key, count) ->
            MapCluster(
                point = GeoPoint(key.first, key.second),
                count = count,
                label = "Loading location...",
            )
        }
}

private suspend fun reverseGeocode(point: GeoPoint): String {
    val url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${point.latitude}&lon=${point.longitude}&zoom=16&addressdetails=1"
    return runCatching {
        val response = window.fetch(url).await()
        val payload = response.json().await().unsafeCast<dynamic>()
        val address = payload.address
        val road = address?.road as? String
        val suburb = address?.suburb as? String ?: address?.neighbourhood as? String
        val district = address?.city_district as? String
        val city = address?.city as? String ?: address?.town as? String ?: address?.village as? String
        listOfNotNull(road, suburb, district, city).distinct().take(3).joinToString(", ")
            .ifBlank { payload.display_name as? String ?: fallbackLabel(point) }
    }.getOrElse { fallbackLabel(point) }
}

private fun fallbackLabel(point: GeoPoint): String =
    "Near ${round(point.latitude * 100.0) / 100.0}, ${round(point.longitude * 100.0) / 100.0}"

private fun googleEmbedUrl(point: GeoPoint): String =
    "https://www.google.com/maps?q=${point.latitude},${point.longitude}&z=15&output=embed"

private fun googleMapsUrl(point: GeoPoint): String =
    "https://www.google.com/maps/search/?api=1&query=${point.latitude},${point.longitude}"

private fun osmUrl(point: GeoPoint): String =
    "https://www.openstreetmap.org/?mlat=${point.latitude}&mlon=${point.longitude}#map=16/${point.latitude}/${point.longitude}"
