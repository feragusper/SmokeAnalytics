package com.feragusper.smokeanalytics.libraries.architecture.domain.extensions

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.concurrent.TimeUnit

private fun firstInstantToday() = LocalDate.now().atStartOfDay()

private fun lastInstantToday() = firstInstantToday().plusDays(1)

private fun firstInstantThisWeek() = firstInstantToday().with(DayOfWeek.MONDAY)

private fun lastInstantThisWeek() = firstInstantToday().with(DayOfWeek.SUNDAY).plusDays(1)

private fun firstInstantThisMonth() = firstInstantToday().withDayOfMonth(1)

private fun lastInstantThisMonth() = with(firstInstantToday()) {
    withDayOfMonth(month.length(toLocalDate().isLeapYear))
}

private fun isBetweenDates(date: LocalDateTime, after: LocalDateTime, before: LocalDateTime) =
    date.isAfter(after) && date.isBefore(before)

fun LocalDateTime.isToday() = isBetweenDates(
    this,
    firstInstantToday(),
    lastInstantToday()
)

fun LocalDateTime.isThisWeek() = isBetweenDates(
    this,
    firstInstantThisWeek(),
    lastInstantThisWeek()
)

fun LocalDateTime.isThisMonth() = isBetweenDates(
    this,
    firstInstantThisMonth(),
    lastInstantThisMonth()
)

fun LocalDateTime?.timeElapsedSinceNow(): Pair<Long, Long> = LocalDateTime.now().timeAfter(this)

fun LocalDateTime.timeAfter(date: LocalDateTime?): Pair<Long, Long> = date?.let { dateNotNull ->
    dateNotNull.until(this, java.time.temporal.ChronoUnit.MILLIS).let { diff ->
        TimeUnit.MILLISECONDS.toHours(diff) % 24 to TimeUnit.MILLISECONDS.toMinutes(diff) % 60
    }
} ?: (0L to 0L)

fun LocalDateTime.timeFormatted(): String = DateTimeFormatter.ofPattern("HH:mm").format(this)

fun LocalDateTime.dateFormatted(): String =
    DateTimeFormatter.ofPattern("EEEE, MMMM dd").format(this)

fun LocalDateTime.utcMillis() = this
    .toEpochSecond(ZoneOffset.UTC)
    .times(1000)

fun LocalDateTime.toDate(): Date = Date.from(this.atZone(ZoneId.systemDefault()).toInstant())

fun LocalDate.toDate(): Date = Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())

fun Date.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault())