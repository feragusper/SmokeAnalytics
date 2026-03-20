package com.feragusper.smokeanalytics.features.history.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryWebStore
import com.feragusper.smokeanalytics.libraries.design.EmptyStateCard
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.InlineErrorCard
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonList
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeRow
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.StatusTone
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
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
    var calendarMode by remember { mutableStateOf(false) }

    val selectedDayStart = state.selectedDate
    val selectedLocalDate = selectedDayStart.toLocalDateTime(tz).date
    val selectedDateLabel = selectedLocalDate.toUiDate()
    val selectedMonthLabel = selectedLocalDate.toUiMonthYear()

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = if (calendarMode) "History · $selectedMonthLabel" else "History for $selectedDateLabel",
            eyebrow = "History",
            badgeText = when {
                state.displayLoading -> "Loading day"
                state.error != null -> "Error"
                else -> "${state.smokes.size} entries"
            },
            badgeTone = when {
                state.displayLoading -> StatusTone.Busy
                state.error != null -> StatusTone.Error
                else -> StatusTone.Default
            },
            actions = {
                PrimaryButton(
                    text = "Add smoke",
                    onClick = { store.send(HistoryIntent.AddSmoke(selectedDayStart)) },
                    enabled = !state.displayLoading
                )
            }
        )

        if (state.error != null) {
            InlineErrorCard(
                title = if (state.error == HistoryResult.Error.NotLoggedIn) "Sign in required" else "History could not be loaded",
                message = when (state.error) {
                    HistoryResult.Error.NotLoggedIn -> "Your session expired. Sign in again to browse and edit smoke history."
                    HistoryResult.Error.Generic -> "The selected day's history could not be loaded. Try refreshing the day."
                    else -> "The selected day's history could not be loaded. Try refreshing the day."
                },
                actionLabel = if (state.error == HistoryResult.Error.NotLoggedIn) "Go to sign in" else "Retry",
                onAction = if (state.error == HistoryResult.Error.NotLoggedIn) {
                    onNavigateToAuth
                } else {
                    { store.send(HistoryIntent.FetchSmokes(selectedDayStart)) }
                },
            )
        }

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.statsToolbar) }) {
                Div(attrs = { classes(SmokeWebStyles.dateControls) }) {
                    GhostButton(
                        text = "←",
                        onClick = {
                            store.send(HistoryIntent.FetchSmokes(selectedDayStart.minusDays(1, tz)))
                        },
                        enabled = !state.displayLoading
                    )

                    Div(attrs = { classes(SmokeWebStyles.dateLabel) }) {
                        Text(selectedDateLabel)
                    }

                    GhostButton(
                        text = "→",
                        onClick = {
                            store.send(HistoryIntent.FetchSmokes(selectedDayStart.plusDays(1, tz)))
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

                    GhostButton(
                        text = if (calendarMode) "List" else "Calendar",
                        onClick = { calendarMode = !calendarMode },
                        enabled = !state.displayLoading
                    )
                }

                Div(attrs = { classes(SmokeWebStyles.statusPill) }) {
                    Text("Smokes: ${state.smokes.size}")
                }
            }
        }

        if (calendarMode) {
            CalendarMonthCard(
                selectedLocalDate = selectedLocalDate,
                monthCounts = state.monthCounts,
                onPickDay = { picked ->
                    store.send(HistoryIntent.FetchSmokes(picked.atStartOfDayIn(tz)))
                }
            )
        }

        when {
            state.displayLoading -> LoadingSkeletonList(rows = if (calendarMode) 4 else 5)
            state.smokes.isEmpty() -> EmptyStateCard(
                title = if (calendarMode) "No smokes for ${selectedLocalDate.toUiDate()}" else "No smokes for this day",
                message = "Shift the date or add a smoke for the selected day.",
                actionLabel = "Add smoke",
                onAction = { store.send(HistoryIntent.AddSmoke(selectedDayStart)) },
            )

            else -> HistorySmokeList(
                state = state,
                tz = tz,
                editing = editing,
                draftDateTime = draftDateTime,
                onEditSmoke = { id, instant -> store.send(HistoryIntent.EditSmoke(id, instant)) },
                onDeleteSmoke = { id -> store.send(HistoryIntent.DeleteSmoke(id)) },
            )
        }
    }
}

@Composable
private fun CalendarMonthCard(
    selectedLocalDate: LocalDate,
    monthCounts: Map<Int, Int>,
    onPickDay: (LocalDate) -> Unit,
) {
    val monthStart = LocalDate(
        year = selectedLocalDate.year,
        monthNumber = selectedLocalDate.monthNumber,
        dayOfMonth = 1,
    )
    val nextMonthStart = monthStart.plus(DatePeriod(months = 1))
    val daysInMonth = nextMonthStart.minus(DatePeriod(days = 1)).dayOfMonth
    val leadingEmptySlots = monthStart.dayOfWeek.mondayBasedIndex()
    val maxCount = monthCounts.values.maxOrNull() ?: 0

    SurfaceCard {
        Div(attrs = { classes(SmokeWebStyles.sectionHeader) }) {
            Div(attrs = { classes(SmokeWebStyles.sectionHeaderText) }) {
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Calendar") }
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                    Text("Month view with daily smoke totals.")
                }
            }
        }

        Div(attrs = { classes(SmokeWebStyles.calendarWeekdays) }) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { label ->
                Div(attrs = { classes(SmokeWebStyles.calendarWeekday) }) { Text(label) }
            }
        }

        Div(attrs = { classes(SmokeWebStyles.calendarGrid) }) {
            repeat(leadingEmptySlots) {
                Div(attrs = { classes(SmokeWebStyles.calendarDaySpacer) })
            }

            for (day in 1..daysInMonth) {
                val date = LocalDate(
                    year = selectedLocalDate.year,
                    monthNumber = selectedLocalDate.monthNumber,
                    dayOfMonth = day,
                )
                val count = monthCounts[day] ?: 0
                val classes = buildList {
                    add(SmokeWebStyles.calendarDay)
                    if (date == selectedLocalDate) add(SmokeWebStyles.calendarDaySelected)
                    if (count > 0) add(calendarDayLevelClass(count = count, maxCount = maxCount))
                }.toTypedArray()

                Div(
                    attrs = {
                        classes(*classes)
                        onClick { onPickDay(date) }
                    }
                ) {
                    Div(attrs = { classes(SmokeWebStyles.calendarDayNumber) }) { Text(day.toString()) }
                    Div(attrs = { classes(SmokeWebStyles.calendarDayMeta) }) {
                        Text(if (count > 0) "$count smokes" else "Clear")
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySmokeList(
    state: HistoryViewState,
    tz: TimeZone,
    editing: MutableMap<String, Boolean>,
    draftDateTime: MutableMap<String, String>,
    onEditSmoke: (String, Instant) -> Unit,
    onDeleteSmoke: (String) -> Unit,
) {
    Div(attrs = { classes(SmokeWebStyles.list) }) {
        state.smokes.forEach { smoke ->
            val id = smoke.id
            val isEditing = editing[id] == true

            val local = smoke.date.toLocalDateTime(tz)
            val hh = local.hour.toString().padStart(2, '0')
            val mm = local.minute.toString().padStart(2, '0')
            val timeLabel = "$hh:$mm"
            val subtitle = smoke.timeElapsedSincePreviousSmoke.let { (h, m) ->
                if (h > 0) "After ${h}h ${m}m" else "After ${m}m"
            }
            val toneClass = when (elapsedToneFrom(
                smoke.timeElapsedSincePreviousSmoke.first,
                smoke.timeElapsedSincePreviousSmoke.second,
            )) {
                ElapsedTone.Urgent -> SmokeWebStyles.listRowUrgent
                ElapsedTone.Warning -> SmokeWebStyles.listRowWarning
                ElapsedTone.Caution -> SmokeWebStyles.listRowCaution
                ElapsedTone.Calm -> SmokeWebStyles.listRowCalm
            }

            if (!isEditing) {
                SmokeRow(
                    time = timeLabel,
                    subtitle = subtitle,
                    toneClass = toneClass,
                    onEdit = {
                        editing[id] = true
                        draftDateTime[id] = smoke.date.toHtmlDateTimeLocal(tz)
                    },
                    onDelete = { onDeleteSmoke(id) }
                )
            } else {
                val draft = draftDateTime[id] ?: smoke.date.toHtmlDateTimeLocal(tz)

                SurfaceCard {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                        Text("Edit smoke")
                    }
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                        Text("${local.date.toUiDate()} ${local.toUiTime()}")
                    }

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

                    Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                        PrimaryButton(
                            text = "Apply",
                            enabled = !state.displayLoading,
                            onClick = {
                                val v = draftDateTime[id] ?: return@PrimaryButton
                                val newInstant =
                                    v.toInstantFromHtmlDateTimeLocalOrNull(tz) ?: return@PrimaryButton
                                onEditSmoke(id, newInstant)
                                editing[id] = false
                            }
                        )
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

private enum class ElapsedTone {
    Urgent,
    Warning,
    Caution,
    Calm,
}

private fun elapsedToneFrom(hours: Long, minutes: Long): ElapsedTone {
    val totalMinutes = (hours * 60L + minutes).toInt()
    return when {
        totalMinutes >= 180 -> ElapsedTone.Calm
        totalMinutes >= 90 -> ElapsedTone.Caution
        totalMinutes >= 45 -> ElapsedTone.Warning
        else -> ElapsedTone.Urgent
    }
}

private fun calendarDayLevelClass(count: Int, maxCount: Int): String {
    if (count <= 0 || maxCount <= 0) return SmokeWebStyles.calendarDayLevel1
    val ratio = count.toDouble() / maxCount.toDouble()
    return when {
        ratio >= 0.75 -> SmokeWebStyles.calendarDayLevel4
        ratio >= 0.5 -> SmokeWebStyles.calendarDayLevel3
        ratio >= 0.25 -> SmokeWebStyles.calendarDayLevel2
        else -> SmokeWebStyles.calendarDayLevel1
    }
}

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

private fun LocalDate.toUiMonthYear(): String {
    val month = month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "$month $year"
}

private fun DayOfWeek.mondayBasedIndex(): Int = isoDayNumber - 1

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
