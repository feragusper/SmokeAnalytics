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
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul

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

    // Per-row edit state
    val editing = remember { mutableStateMapOf<String, Boolean>() }
    val draftDateTime = remember { mutableStateMapOf<String, String>() }

    // ✅ Normalize selected day to 00:00 so all "day actions" are consistent.
    val selectedDayStart = state.selectedDate.dayStart(tz)
    val selectedLocalDate = selectedDayStart.toLocalDateTime(tz).date
    val selectedDateLabel = selectedLocalDate.toUiDate()

    Div {
        H3 { Text("History • $selectedDateLabel") }

        if (state.displayLoading) {
            Div { Text("Loading...") }
        }

        state.error?.let { err ->
            Div {
                Text(
                    when (err) {
                        HistoryResult.Error.NotLoggedIn -> "Not logged in"
                        HistoryResult.Error.Generic -> "Something went wrong"
                    }
                )
            }

            if (err == HistoryResult.Error.NotLoggedIn) {
                Button(attrs = { onClick { onNavigateToAuth() } }) { Text("Go to sign in") }
            }
        }

        // Controls
        Div {
            Button(attrs = { onClick { store.send(HistoryIntent.NavigateUp); onNavigateUp() } }) {
                Text("Back")
            }

            Span { Text("  ") }

            Button(
                attrs = {
                    if (state.displayLoading) disabled()
                    onClick { store.send(HistoryIntent.AddSmoke(selectedDayStart)) }
                }
            ) { Text("Add smoke") }

            Span { Text("  ") }

            Button(
                attrs = {
                    if (state.displayLoading) disabled()
                    onClick { store.send(HistoryIntent.FetchSmokes(selectedDayStart)) }
                }
            ) { Text("Refresh") }
        }

        // Day navigation + day picker
        Div {
            Button(
                attrs = {
                    if (state.displayLoading) disabled()
                    onClick {
                        store.send(
                            HistoryIntent.FetchSmokes(
                                selectedDayStart.minusDays(1, tz)
                            )
                        )
                    }
                }
            ) { Text("←") }

            Span { Text("  ") }

            Input(
                type = InputType.Date,
                attrs = {
                    value(selectedLocalDate.toHtmlDate())
                    if (state.displayLoading) disabled()
                    onInput { e ->
                        val picked = (e.value ?: "").toLocalDateOrNull() ?: return@onInput
                        store.send(HistoryIntent.FetchSmokes(picked.atStartOfDayIn(tz)))
                    }
                }
            )

            Span { Text("  ") }

            Button(
                attrs = {
                    if (state.displayLoading) disabled()
                    onClick {
                        store.send(
                            HistoryIntent.FetchSmokes(
                                selectedDayStart.plusDays(1, tz)
                            )
                        )
                    }
                }
            ) { Text("→") }
        }

        Div { Text("Smokes: ${state.smokes.size}") }

        Ul {
            state.smokes.forEach { smoke ->
                val id = smoke.id
                val isEditing = editing[id] == true

                val local = smoke.date.toLocalDateTime(tz)
                val label = "${local.date.toUiDate()} ${local.toUiTime()}"

                Li {
                    Text(label)
                    Span { Text("  ") }

                    if (!isEditing) {
                        Button(
                            attrs = {
                                if (state.displayLoading) disabled()
                                onClick {
                                    editing[id] = true
                                    draftDateTime[id] = smoke.date.toHtmlDateTimeLocal(tz)
                                }
                            }
                        ) { Text("Edit") }

                        Span { Text("  ") }

                        Button(
                            attrs = {
                                if (state.displayLoading) disabled()
                                onClick { store.send(HistoryIntent.DeleteSmoke(id)) }
                            }
                        ) { Text("Delete") }
                    } else {
                        val draft = draftDateTime[id] ?: smoke.date.toHtmlDateTimeLocal(tz)

                        Input(
                            type = InputType.DateTimeLocal,
                            attrs = {
                                value(draft)
                                if (state.displayLoading) disabled()
                                onInput { ev ->
                                    draftDateTime[id] = ev.value ?: draft
                                }
                            }
                        )

                        Span { Text("  ") }

                        Button(
                            attrs = {
                                if (state.displayLoading) disabled()
                                onClick {
                                    val v = draftDateTime[id] ?: return@onClick
                                    val newInstant =
                                        v.toInstantFromHtmlDateTimeLocalOrNull(tz) ?: return@onClick
                                    store.send(HistoryIntent.EditSmoke(id, newInstant))
                                    editing[id] = false
                                }
                            }
                        ) { Text("Apply") }

                        Span { Text("  ") }

                        Button(
                            attrs = {
                                if (state.displayLoading) disabled()
                                onClick {
                                    editing[id] = false
                                    draftDateTime.remove(id)
                                }
                            }
                        ) { Text("Cancel") }
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