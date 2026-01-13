package com.feragusper.smokeanalytics.features.history.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryWebStore
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeRow
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun HistoryWebScreen(
    deps: HistoryWebDependencies,
    onNavigateUp: () -> Unit,
    onNavigateToAuth: () -> Unit,
) {
    val store = remember(deps) { HistoryWebStore(deps.historyProcessHolder) }
    LaunchedEffect(store) { store.start() }

    val state by store.state.collectAsState()
    val tz = remember { TimeZone.currentSystemDefault() }

    val editing = remember { mutableStateMapOf<String, Boolean>() }
    val draftDateTime = remember { mutableStateMapOf<String, String>() }

    val selectedDayStart = state.selectedDate.dayStart(tz)
    val selectedLocalDate = selectedDayStart.toLocalDateTime(tz).date
    val selectedDateLabel = selectedLocalDate.toUiDate()

    Div(attrs = { classes(SmokeWebStyles.mainInner) }) {

        // Title
        Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
            Text("History • $selectedDateLabel")
        }

        // Errors
        state.error?.let { err ->
            SurfaceCard {
                Text(
                    when (err) {
                        HistoryResult.Error.NotLoggedIn -> "Not logged in"
                        HistoryResult.Error.Generic -> "Something went wrong"
                    }
                )

                if (err == HistoryResult.Error.NotLoggedIn) {
                    Div {
                        PrimaryButton(
                            text = "Go to sign in",
                            onClick = onNavigateToAuth,
                            enabled = !state.displayLoading
                        )
                    }
                }
            }
        }

        // Top actions (Back / Add / Refresh)
        Div(attrs = { classes(SmokeWebStyles.statsToolbar) }) {

            Div {
                GhostButton(
                    text = "Back",
                    onClick = {
                        store.send(HistoryIntent.NavigateUp)
                        onNavigateUp()
                    },
                    enabled = !state.displayLoading
                )
            }

            Div {
                PrimaryButton(
                    text = "Add smoke",
                    onClick = { store.send(HistoryIntent.AddSmoke(selectedDayStart)) },
                    enabled = !state.displayLoading
                )
                Span { Text(" ") }
                GhostButton(
                    text = "Refresh",
                    onClick = { store.send(HistoryIntent.FetchSmokes(selectedDayStart)) },
                    enabled = !state.displayLoading
                )
            }
        }

        // Day navigation + picker
        Div(attrs = { classes(SmokeWebStyles.statsToolbar) }) {
            Div(attrs = { classes(SmokeWebStyles.dateControls) }) {

                GhostButton(
                    text = "←",
                    onClick = {
                        store.send(
                            HistoryIntent.FetchSmokes(
                                selectedDayStart.minusDays(1, tz)
                            )
                        )
                    },
                    enabled = !state.displayLoading
                )

                Div(attrs = { classes(SmokeWebStyles.dateLabel) }) {
                    Text(selectedDateLabel)
                }

                GhostButton(
                    text = "→",
                    onClick = {
                        store.send(
                            HistoryIntent.FetchSmokes(
                                selectedDayStart.plusDays(1, tz)
                            )
                        )
                    },
                    enabled = !state.displayLoading
                )

                Input(
                    type = InputType.Date,
                    attrs = {
                        classes(SmokeWebStyles.dateInput)
                        value(selectedLocalDate.toHtmlDate())
                        if (state.displayLoading) disabled()
                        onInput { e ->
                            val picked = e.value.toLocalDateOrNull() ?: return@onInput
                            store.send(HistoryIntent.FetchSmokes(picked.atStartOfDayIn(tz)))
                        }
                    }
                )
            }

            Div {
                Text("Smokes: ${state.smokes.size}")
            }
        }

        if (state.displayLoading) {
            SurfaceCard { Text("Loading...") }
        } else if (state.smokes.isEmpty()) {
            SurfaceCard { Text("No smokes for this day.") }
        } else {
            Div(attrs = { classes(SmokeWebStyles.list) }) {
                state.smokes.forEach { smoke ->
                    val id = smoke.id
                    val isEditing = editing[id] == true

                    val local = smoke.date.toLocalDateTime(tz)
                    val hh = local.hour.toString().padStart(2, '0')
                    val mm = local.minute.toString().padStart(2, '0')
                    val timeLabel = "$hh:$mm"
                    val subtitle = local.date.toUiDate()

                    if (!isEditing) {
                        SmokeRow(
                            time = timeLabel,
                            subtitle = subtitle,
                            onEdit = {
                                editing[id] = true
                                draftDateTime[id] = smoke.date.toHtmlDateTimeLocal(tz)
                            },
                            onDelete = { store.send(HistoryIntent.DeleteSmoke(id)) }
                        )
                    } else {
                        val draft = draftDateTime[id] ?: smoke.date.toHtmlDateTimeLocal(tz)

                        SurfaceCard {
                            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                                Text("Edit smoke")
                            }

                            P { Text("Current: ${local.date.toUiDate()} ${local.toUiTime()}") }

                            Input(
                                type = InputType.DateTimeLocal,
                                attrs = {
                                    classes(SmokeWebStyles.dateInput)
                                    value(draft)
                                    if (state.displayLoading) disabled()
                                    onInput { ev ->
                                        draftDateTime[id] = ev.value
                                    }
                                }
                            )

                            Div {
                                PrimaryButton(
                                    text = "Apply",
                                    enabled = !state.displayLoading,
                                    onClick = {
                                        val v = draftDateTime[id] ?: return@PrimaryButton
                                        val newInstant =
                                            v.toInstantFromHtmlDateTimeLocalOrNull(tz) ?: return@PrimaryButton
                                        store.send(HistoryIntent.EditSmoke(id, newInstant))
                                        editing[id] = false
                                    }
                                )
                                Span { Text(" ") }
                                GhostButton(
                                    text = "Cancel",
                                    enabled = !state.displayLoading,
                                    onClick = {
                                        editing[id] = false
                                        draftDateTime.remove(id)
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

private fun Instant.dayStart(timeZone: TimeZone): Instant =
    toLocalDateTime(timeZone).date.atStartOfDayIn(timeZone)

private fun Instant.plusDays(days: Int, timeZone: TimeZone): Instant =
    this.plus(days, DateTimeUnit.DAY, timeZone)

private fun Instant.minusDays(days: Int, timeZone: TimeZone): Instant =
    this.minus(days, DateTimeUnit.DAY, timeZone)

private fun LocalDate.toHtmlDate(): String {
    val y = year.toString().padStart(4, '0')
    val m = monthNumber.toString().padStart(2, '0')
    val d = dayOfMonth.toString().padStart(2, '0')
    return "$y-$m-$d"
}

private fun LocalDate.toUiDate(): String {
    val d = dayOfMonth.toString().padStart(2, '0')
    val m = monthNumber.toString().padStart(2, '0')
    return "$d/$m/$year"
}

private fun LocalDateTime.toUiTime(): String {
    val h = hour.toString().padStart(2, '0')
    val m = minute.toString().padStart(2, '0')
    return "$h:$m"
}

private fun Instant.toHtmlDateTimeLocal(timeZone: TimeZone): String {
    val ldt = toLocalDateTime(timeZone)
    val y = ldt.year.toString().padStart(4, '0')
    val mo = ldt.monthNumber.toString().padStart(2, '0')
    val d = ldt.dayOfMonth.toString().padStart(2, '0')
    val h = ldt.hour.toString().padStart(2, '0')
    val mi = ldt.minute.toString().padStart(2, '0')
    return "$y-$mo-$d" + "T" + "$h:$mi"
}

private fun String.toLocalDateOrNull(): LocalDate? {
    if (length != 10) return null
    val y = substring(0, 4).toIntOrNull() ?: return null
    val m = substring(5, 7).toIntOrNull() ?: return null
    val d = substring(8, 10).toIntOrNull() ?: return null
    return runCatching { LocalDate(y, m, d) }.getOrNull()
}

private fun String.toInstantFromHtmlDateTimeLocalOrNull(timeZone: TimeZone): Instant? {
    if (length < 16) return null
    val y = substring(0, 4).toIntOrNull() ?: return null
    val mo = substring(5, 7).toIntOrNull() ?: return null
    val d = substring(8, 10).toIntOrNull() ?: return null
    val h = substring(11, 13).toIntOrNull() ?: return null
    val mi = substring(14, 16).toIntOrNull() ?: return null

    val ldt = LocalDateTime(
        year = y,
        monthNumber = mo,
        dayOfMonth = d,
        hour = h,
        minute = mi,
        second = 0,
        nanosecond = 0,
    )

    return runCatching { ldt.toInstant(timeZone) }.getOrNull()
}