package com.feragusper.smokeanalytics.features.home.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeWebStore
import com.feragusper.smokeanalytics.libraries.design.EmptyStateCard
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.InlineErrorCard
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonCard
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonList
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeRow
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.StatCard
import com.feragusper.smokeanalytics.libraries.design.StatusTone
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text

@Composable
fun HomeWebScreen(
    deps: HomeWebDependencies,
    onNavigateToHistory: () -> Unit,
) {
    val store = remember(deps) { HomeWebStore(processHolder = deps.homeProcessHolder) }

    LaunchedEffect(store) { store.start() }

    val state by store.state.collectAsState()

    state.Render(
        onIntent = { intent ->
            when (intent) {
                HomeIntent.OnClickHistory -> onNavigateToHistory()
                else -> store.send(intent)
            }
        }
    )
}

@Composable
fun HomeViewState.Render(
    onIntent: (HomeIntent) -> Unit,
) {
    val tz = remember { TimeZone.currentSystemDefault() }
    val editing = remember { mutableStateMapOf<String, Boolean>() }
    val draftTime = remember { mutableStateMapOf<String, String>() }
    val showingInitialSkeleton = displayLoading && latestSmokes == null

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "Daily snapshot",
            subtitle = "Start from the latest activity, keep quick actions nearby, and make refreshes feel calm instead of disruptive.",
            eyebrow = "Home",
            badgeText = when {
                displayRefreshLoading -> "Refreshing"
                error != null -> "Needs attention"
                else -> "Live"
            },
            badgeTone = when {
                displayRefreshLoading -> StatusTone.Busy
                error != null -> StatusTone.Error
                else -> StatusTone.Default
            },
            actions = {
                PrimaryButton(
                    text = "Add smoke",
                    onClick = { onIntent(HomeIntent.AddSmoke) },
                    enabled = !displayLoading,
                )
                GhostButton(
                    text = "History",
                    onClick = { onIntent(HomeIntent.OnClickHistory) },
                    enabled = !displayLoading,
                )
                GhostButton(
                    text = "Refresh",
                    onClick = { onIntent(HomeIntent.RefreshFetchSmokes) },
                    enabled = !displayLoading,
                )
            }
        )

        if (error != null) {
            InlineErrorCard(
                title = if (error == HomeViewState.HomeError.NotLoggedIn) "Session required" else "Could not refresh home",
                message = when (error) {
                    HomeViewState.HomeError.NotLoggedIn -> "Sign in from Settings to sync the latest smoke entries on the web."
                    HomeViewState.HomeError.Generic -> "The home dashboard could not be refreshed. Try again in a moment."
                },
                actionLabel = "Retry",
                onAction = { onIntent(HomeIntent.RefreshFetchSmokes) },
            )
        }

        if (showingInitialSkeleton) {
            Div(attrs = { classes(SmokeWebStyles.skeletonGrid) }) {
                repeat(3) { LoadingSkeletonCard(heightPx = 96, lineWidths = listOf("36%", "64%")) }
            }
            LoadingSkeletonCard(heightPx = 110, lineWidths = listOf("28%", "42%"))
            LoadingSkeletonList(rows = 4)
        } else {
            Div(
                attrs = {
                    classes(SmokeWebStyles.statsRow)
                    if (displayRefreshLoading) classes(SmokeWebStyles.surfaceMuted)
                }
            ) {
                StatCard(
                    title = "Today",
                    value = smokesPerDay?.toString() ?: "--",
                    onClick = { onIntent(HomeIntent.OnClickHistory) }
                )
                StatCard(
                    title = "This week",
                    value = smokesPerWeek?.toString() ?: "--",
                    onClick = { onIntent(HomeIntent.OnClickHistory) }
                )
                StatCard(
                    title = "This month",
                    value = smokesPerMonth?.toString() ?: "--",
                    onClick = { onIntent(HomeIntent.OnClickHistory) }
                )
            }

            Div(
                attrs = {
                    classes(SmokeWebStyles.sectionHeader)
                    if (displayRefreshLoading) classes(SmokeWebStyles.surfaceMuted)
                }
            ) {
                Div(attrs = { classes(SmokeWebStyles.sectionHeaderText) }) {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                        Text("Since your last cigarette")
                    }
                    Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                        Text("A quick signal for the current streak without leaving the dashboard.")
                    }
                }
            }

            SurfaceCard(*(if (displayRefreshLoading) arrayOf(SmokeWebStyles.surfaceMuted) else emptyArray())) {
                val since = timeSinceLastCigarette?.let { (h, m) ->
                    buildString {
                        if (h > 0) append("${h}h, ")
                        append("${m}m")
                    }
                } ?: "--"

                Div(attrs = { classes(SmokeWebStyles.sinceValue) }) { Text(since) }
                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                    Text(if (displayRefreshLoading) "Refreshing latest streak..." else "Keep the rhythm visible to reduce guesswork.")
                }
            }

            Div(attrs = { classes(SmokeWebStyles.sectionHeader) }) {
                Div(attrs = { classes(SmokeWebStyles.sectionHeaderText) }) {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Smoked today") }
                    Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                        Text("Review the latest entries, edit time stamps, or jump into the full history.")
                    }
                }
            }

            when {
                latestSmokes.isNullOrEmpty() -> EmptyStateCard(
                    title = "No smokes logged today",
                    message = "When you add the next entry it will appear here immediately, with quick edit and delete controls.",
                    actionLabel = "Add smoke",
                    onAction = { onIntent(HomeIntent.AddSmoke) },
                )

                else -> Div(
                    attrs = {
                        classes(SmokeWebStyles.list)
                        if (displayRefreshLoading) classes(SmokeWebStyles.surfaceMuted)
                    }
                ) {
                    latestSmokes.forEach { smoke ->
                        val id = smoke.id
                        val isEditing = editing[id] == true
                        val local = smoke.date.toLocalDateTime(tz)
                        val hh = local.hour.toString().padStart(2, '0')
                        val mm = local.minute.toString().padStart(2, '0')
                        val timeLabel = "$hh:$mm"
                        val subtitle = smoke.timeElapsedSincePreviousSmoke.let { (h, m) ->
                            if (h > 0) "After $h hours and $m minutes" else "After $m minutes"
                        }

                        if (!isEditing) {
                            SmokeRow(
                                time = timeLabel,
                                subtitle = subtitle,
                                onEdit = {
                                    editing[id] = true
                                    draftTime[id] = smoke.date.toTimeInputValue(tz)
                                },
                                onDelete = { onIntent(HomeIntent.DeleteSmoke(id)) }
                            )
                        } else {
                            val draft = draftTime[id] ?: smoke.date.toTimeInputValue(tz)

                            SurfaceCard {
                                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                                    Text("Edit smoke time")
                                }

                                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                                    Text("Adjust the time without leaving the dashboard.")
                                }

                                Input(
                                    type = org.jetbrains.compose.web.attributes.InputType.Time,
                                    attrs = {
                                        classes(SmokeWebStyles.dateInput)
                                        value(draft)
                                        if (displayLoading || displayRefreshLoading) disabled()
                                        onInput { ev -> draftTime[id] = ev.value }
                                    }
                                )

                                Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                                    PrimaryButton(
                                        text = "Apply",
                                        enabled = !displayLoading && !displayRefreshLoading,
                                        onClick = {
                                            val newTime = draftTime[id] ?: return@PrimaryButton
                                            val dateValue = smoke.date.toDateInputValue(tz)

                                            val newInstant = dateTimeInputsToInstant(
                                                dateValue = dateValue,
                                                timeValue = newTime,
                                                timeZone = tz,
                                            )

                                            onIntent(HomeIntent.EditSmoke(id, newInstant))
                                            editing[id] = false
                                            draftTime.remove(id)
                                        }
                                    )
                                    GhostButton(
                                        text = "Cancel",
                                        enabled = !displayLoading && !displayRefreshLoading,
                                        onClick = {
                                            editing[id] = false
                                            draftTime.remove(id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun Instant.toDateInputValue(timeZone: TimeZone): String {
    val ldt = toLocalDateTime(timeZone)
    val mm = ldt.monthNumber.toString().padStart(2, '0')
    val dd = ldt.dayOfMonth.toString().padStart(2, '0')
    return "${ldt.year}-$mm-$dd"
}

internal fun Instant.toTimeInputValue(timeZone: TimeZone): String {
    val ldt = toLocalDateTime(timeZone)
    val hh = ldt.hour.toString().padStart(2, '0')
    val mm = ldt.minute.toString().padStart(2, '0')
    return "$hh:$mm"
}

internal fun dateTimeInputsToInstant(
    dateValue: String,
    timeValue: String,
    timeZone: TimeZone,
): Instant {
    val date = LocalDate.parse(dateValue)
    val time = LocalTime.parse(timeValue)
    val ldt = LocalDateTime(
        year = date.year,
        monthNumber = date.monthNumber,
        dayOfMonth = date.dayOfMonth,
        hour = time.hour,
        minute = time.minute,
        second = 0,
        nanosecond = 0,
    )
    return ldt.toInstant(timeZone)
}
