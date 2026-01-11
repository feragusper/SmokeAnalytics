package com.feragusper.smokeanalytics.features.home.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeResult
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeWebStore
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeRow
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.StatCard
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

/**
 * Represents the dependencies required by the [HomeWebScreen].
 *
 * @param deps Dependencies required by the [HomeWebScreen].
 * @param onNavigateToHistory Callback to navigate to the history screen.
 */
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

/**
 * Represents the dependencies required by the [HomeWebScreen].
 *
 * @param onIntent Callback to send intents to the [HomeWebStore].
 */
@Composable
fun HomeViewState.Render(
    onIntent: (HomeIntent) -> Unit,
) {
    Div {
        Div(attrs = { classes(SmokeWebStyles.statsRow) }) {
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

        Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Since your last cigarette") }

        SurfaceCard {
            val since = timeSinceLastCigarette?.let { (h, m) ->
                buildString {
                    if (h > 0) append("${h}h, ")
                    append("${m}m")
                }
            } ?: "--"

            Div(attrs = { classes(SmokeWebStyles.sinceValue) }) { Text(since) }
        }

        Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Smoked today") }

        if (displayLoading) {
            P { Text("Loading...") }
        } else if (latestSmokes.isNullOrEmpty()) {
            P { Text("No smokes") }
        } else {
            Div(attrs = { classes(SmokeWebStyles.list) }) {
                latestSmokes.forEach { smoke ->
                    val local = smoke.date.toLocalDateTime(TimeZone.currentSystemDefault())
                    val hh = local.hour.toString().padStart(2, '0')
                    val mm = local.minute.toString().padStart(2, '0')
                    val subtitle = smoke.timeElapsedSincePreviousSmoke.let { (h, m) ->
                        if (h > 0) "After $h hours and $m minutes" else "After $m minutes"
                    }

                    SmokeRow(
                        time = "$hh:$mm",
                        subtitle = subtitle,
                        onEdit = { onIntent(HomeIntent.EditSmoke(smoke.id, smoke.date)) },
                        onDelete = { onIntent(HomeIntent.DeleteSmoke(smoke.id)) }
                    )
                }
            }
        }

        Div {
            PrimaryButton(
                text = "Add smoke",
                onClick = { onIntent(HomeIntent.AddSmoke) },
                enabled = !displayLoading
            )
            Span { Text(" ") }
            GhostButton(
                text = "Refresh",
                onClick = { onIntent(HomeIntent.RefreshFetchSmokes) },
                enabled = !displayLoading
            )
            Span { Text(" ") }
            GhostButton(
                text = "History",
                onClick = { onIntent(HomeIntent.OnClickHistory) },
                enabled = !displayLoading
            )
        }

        if (error != null) {
            P {
                Text(
                    when (error) {
                        HomeResult.Error.NotLoggedIn -> "Not logged in"
                        HomeResult.Error.Generic -> "Something went wrong"
                        else -> "Unknown error"
                    }
                )
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