package com.feragusper.smokeanalytics.features.home.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun HomeWebScreen(
    deps: HomeWebDependencies,
    onNavigateToAuth: () -> Unit,
    onNavigateToHistory: () -> Unit,
) {
    val store = remember(deps) { HomeWebStore(processHolder = deps.homeProcessHolder) }

    LaunchedEffect(store) { store.start() }

    val state by store.state.collectAsState()

    state.Render(
        onNavigateToHistory = onNavigateToHistory,
        onIntent = { intent ->
            when (intent) {
                HomeIntent.OnClickHistory -> onNavigateToHistory()
//                HomeIntent.GoToAuthentication -> onNavigateToAuth()
                else -> store.send(intent)
            }
        }
    )
}

@Composable
fun HomeViewState.Render(
    onNavigateToHistory: () -> Unit,
    onIntent: (HomeIntent) -> Unit,
) {
    Div {
        H2 { Text("Home") }

        if (displayLoading) {
            P { Text("Loading...") }
        }

        Div {
            Button(
                attrs = {
                    if (displayLoading) disabled()
                    onClick { onIntent(HomeIntent.AddSmoke) }
                }
            ) { Text("Add smoke") }

            Text(" ")

            Button(
                attrs = {
                    if (displayLoading) disabled()
                    onClick { onIntent(HomeIntent.RefreshFetchSmokes) }
                }
            ) { Text("Refresh") }
        }

        Hr()

        P { Text("Today: ${smokesPerDay ?: "--"}") }
        P { Text("Week: ${smokesPerWeek ?: "--"}") }
        P { Text("Month: ${smokesPerMonth ?: "--"}") }

        val since = timeSinceLastCigarette?.let { (h, m) -> "${h}h ${m}m" } ?: "--"
        P { Text("Since last: $since") }

        Hr()

        var editing by remember { mutableStateOf<Smoke?>(null) }

        latestSmokes?.let { smokes ->
            Hr()
            H2 { Text("Smoked today") }

            if (smokes.isEmpty()) {
                P { Text("No smokes yet") }
            } else {
                smokes.forEach { smoke ->
                    val local = smoke.date.toLocalDateTime(TimeZone.currentSystemDefault())
                    Div {
                        P {
                            val hh = local.hour.toString().padStart(2, '0')
                            val mm = local.minute.toString().padStart(2, '0')

                            Text("$hh:$mm")
                            Text("  (id=${smoke.id})")
                        }

                        Button(
                            attrs = {
                                if (displayLoading) disabled()
                                onClick { editing = smoke }
                            }
                        ) { Text("Edit") }

                        Text(" ")

                        Button(
                            attrs = {
                                if (displayLoading) disabled()
                                onClick { onIntent(HomeIntent.DeleteSmoke(smoke.id)) }
                            }
                        ) { Text("Delete") }

                        Hr()
                    }
                }
            }
        }

        editing?.let { smoke ->
            EditSmokeDialogWeb(
                initialInstant = smoke.date,
                fullDateTimeEdit = false, // igual que mobile Home (solo hora)
                onDismiss = { editing = null },
                onConfirm = { newInstant ->
                    editing = null
                    onIntent(HomeIntent.EditSmoke(smoke.id, newInstant))
                }
            )
        }

        Button(attrs = { onClick { onIntent(HomeIntent.OnClickHistory) } }) {
            Text("History")
        }

        if (error != null) {
            Hr()
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
    val date = LocalDate.parse(dateValue)          // "YYYY-MM-DD"
    val time = LocalTime.parse(timeValue)          // "HH:MM"
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