package com.feragusper.smokeanalytics.apps.web

import androidx.compose.runtime.Composable
import com.feragusper.smokeanalytics.libraries.design.EmptyStateCard
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonCard
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeMapPeriod
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Iframe
import org.jetbrains.compose.web.dom.Text

@Composable
fun MapWebScreen(
    stateHolder: MapWebStateHolder,
    embedded: Boolean = false,
) {
    val state = stateHolder.state

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        if (!embedded) {
            PageSectionHeader(
                title = "Geographic Clusters",
                eyebrow = "Locations",
                badgeText = if (state.isRefreshing) "Refreshing" else "${state.clusters.sumOf { it.count }} smokes",
                subtitle = "Inspect repeated smoking areas and the places that dominate the current map period.",
                actions = {
                    SmokeMapPeriod.entries.forEach { candidate ->
                        PrimaryButton(
                            text = candidate.name,
                            onClick = { stateHolder.onPeriodChange(candidate) },
                            enabled = !state.isLoading && candidate != state.period,
                        )
                    }
                }
            )
        } else {
            SurfaceCard {
                Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                        Text(
                            if (state.isRefreshing) {
                                "Refreshing clusters in background."
                            } else {
                                "${state.clusters.sumOf { it.count }} mapped smokes in the selected period."
                            }
                        )
                    }
                    Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                        SmokeMapPeriod.entries.forEach { candidate ->
                            PrimaryButton(
                                text = candidate.name,
                                onClick = { stateHolder.onPeriodChange(candidate) },
                                enabled = !state.isLoading && candidate != state.period,
                            )
                        }
                    }
                }
            }
        }

        when {
            state.isLoading -> LoadingSkeletonCard(heightPx = 320, lineWidths = listOf("50%", "30%"))
            state.preferences?.locationTrackingEnabled == false -> EmptyStateCard(
                title = "Location tracking is off",
                message = "Enable location tracking in You to unlock map insights, repeated-area detection, and the geographic side of Analytics.",
            )
            state.clusters.isEmpty() -> EmptyStateCard(
                title = "No mapped smokes yet",
                message = "There is not enough location-linked history for this period yet. Add more smoke entries with location tracking enabled to build clusters.",
            )

            else -> {
                val activeCluster = state.selectedCluster ?: state.clusters.first()

                Div(attrs = {
                    attr("style", "display:grid;grid-template-columns:minmax(0,1.8fr) minmax(280px,1fr);gap:16px;align-items:start;")
                }) {
                    SurfaceCard {
                        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;") }) {
                            if (state.isRefreshing) {
                                Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text("Refreshing clusters in background.") }
                            } else if (state.error) {
                                Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text("Latest refresh failed. Showing the last available clusters.") }
                            }
                            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(activeCluster.label) }
                            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                                Text("${activeCluster.count} smokes grouped in an approximate ${activeCluster.radiusMeters} m area.")
                            }
                            Div(attrs = { attr("style", "font-size:12px;font-weight:700;text-transform:uppercase;color:#7A4A04;") }) {
                                Text("Most frequent area for the selected period")
                            }
                            Div(attrs = {
                                attr("style", "position:relative;width:100%;height:400px;border-radius:24px;overflow:hidden;background:#f5f8f8;")
                            }) {
                                Iframe(attrs = {
                                    attr("src", googleEmbedUrl(activeCluster.point))
                                    attr("loading", "lazy")
                                    attr("referrerpolicy", "no-referrer-when-downgrade")
                                    attr("style", "position:absolute;inset:0;width:100%;height:100%;border:0;")
                                })
                                Div(attrs = {
                                    attr("style", "position:absolute;left:50%;top:50%;width:150px;height:150px;border-radius:999px;border:3px solid rgba(0,106,106,0.82);background:rgba(0,106,106,0.12);transform:translate(-50%,-50%);box-shadow:0 0 0 16px rgba(0,106,106,0.08);pointer-events:none;")
                                })
                                Div(attrs = {
                                    attr("style", "position:absolute;left:50%;top:50%;width:16px;height:16px;border-radius:999px;background:#006A6A;transform:translate(-50%,-50%);box-shadow:0 0 0 6px rgba(255,255,255,0.92);pointer-events:none;")
                                })
                            }
                        }
                    }

                    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
                        SurfaceCard {
                            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Top Clusters") }
                            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                                Text("Pick an area to inspect on Google Maps.")
                            }
                            state.clusters.take(4).forEach { cluster ->
                                Div(
                                    attrs = {
                                        classes(SmokeWebStyles.listRow)
                                        onClick { stateHolder.onSelectCluster(cluster) }
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
                                        onClick = { stateHolder.onSelectCluster(cluster) },
                                        enabled = cluster != activeCluster,
                                    )
                                }
                            }
                        }

                        SurfaceCard {
                            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Observation") }
                            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                                Text("Repeated clusters usually point to routines worth protecting or interrupting, especially around commute, breaks, or end-of-day transitions.")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun googleEmbedUrl(point: GeoPoint): String =
    "https://maps.google.com/maps?ll=${point.latitude},${point.longitude}&z=14&output=embed"
