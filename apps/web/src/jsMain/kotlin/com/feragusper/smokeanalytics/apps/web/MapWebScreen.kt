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
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeMapCluster
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeMapPeriod
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.clusterSmokesForMap
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.smokeMapRange
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Iframe
import org.jetbrains.compose.web.dom.Text

@Composable
fun MapWebScreen(
    fetchSmokesUseCase: FetchSmokesUseCase,
    fetchUserPreferencesUseCase: FetchUserPreferencesUseCase,
) {
    var period by remember { mutableStateOf(SmokeMapPeriod.Week) }
    var clusters by remember { mutableStateOf<List<SmokeMapCluster>>(emptyList()) }
    var selectedCluster by remember { mutableStateOf<SmokeMapCluster?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(period) {
        loading = true
        val preferences = fetchUserPreferencesUseCase()
        val (start, end) = smokeMapRange(
            period = period,
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        clusters = clusterSmokesForMap(fetchSmokesUseCase(start, end), period)
        selectedCluster = clusters.maxByOrNull { it.count }
        loading = false
    }

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "Map",
            eyebrow = "Locations",
            badgeText = "${clusters.sumOf { it.count }} smokes",
            actions = {
                SmokeMapPeriod.entries.forEach { candidate ->
                    PrimaryButton(
                        text = candidate.name,
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
                        Text("${activeCluster.count} smokes grouped in an approximate ${activeCluster.radiusMeters} m area.")
                    }
                    Iframe(attrs = {
                        attr("src", googleEmbedUrl(activeCluster.point))
                        attr("loading", "lazy")
                        attr("referrerpolicy", "no-referrer-when-downgrade")
                        attr("style", "width:100%;height:360px;border:0;border-radius:16px;margin-top:12px;background:#f5f8f8;")
                    })
                }

                SurfaceCard {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Areas") }
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                        Text("Pick an area to inspect on Google Maps.")
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
                                    Text("${cluster.count} smokes in this area")
                                }
                            }
                            PrimaryButton(
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

private fun googleEmbedUrl(point: GeoPoint): String =
    "https://www.google.com/maps?q=${point.latitude},${point.longitude}&z=14&output=embed"
