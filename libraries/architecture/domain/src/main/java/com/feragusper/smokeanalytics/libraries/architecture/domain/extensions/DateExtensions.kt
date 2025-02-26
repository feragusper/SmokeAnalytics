package com.feragusper.smokeanalytics.libraries.architecture.domain.extensions

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Returns the start of the current day as a [LocalDateTime].
 */
private fun firstInstantToday() = LocalDate.now().atStartOfDay()

/**
 * Returns the start of the next day as a [LocalDateTime].
 */
fun lastInstantToday(): LocalDateTime = firstInstantToday().plusDays(1)

/**
 * Returns the first moment of the current month as a [LocalDateTime].
 */
fun firstInstantThisMonth(): LocalDateTime = firstInstantToday().withDayOfMonth(1)

/**
 * Checks if a [LocalDateTime] is within the current day.
 */
fun LocalDateTime.isToday() = isBetweenDates(
    this,
    firstInstantToday(),
    lastInstantToday()
)

/**
 * Checks if a [LocalDateTime] is within the current week.
 */
fun LocalDateTime.isThisWeek() = isBetweenDates(
    this,
    firstInstantThisWeek(),
    lastInstantThisWeek()
)

/**
 * Checks if a [LocalDateTime] is within the current month.
 */
fun LocalDateTime.isThisMonth() = isBetweenDates(
    this,
    firstInstantThisMonth(),
    lastInstantThisMonth()
)

/**
 * Calculates the time elapsed since a given [LocalDateTime] until now.
 *
 * @return A pair of Long values representing hours and minutes elapsed.
 */
fun LocalDateTime?.timeElapsedSinceNow(): Pair<Long, Long> = LocalDateTime.now().timeAfter(this)

/**
 * Calculates the time after a given [LocalDateTime].
 *
 * @param date The date from which to calculate the time after.
 * @return A pair of Long values representing hours and minutes.
 */
fun LocalDateTime.timeAfter(date: LocalDateTime?): Pair<Long, Long> = date?.let { dateNotNull ->
    dateNotNull.until(this, java.time.temporal.ChronoUnit.MILLIS).let { diff ->
        TimeUnit.MILLISECONDS.toHours(diff) % 24 to TimeUnit.MILLISECONDS.toMinutes(diff) % 60
    }
} ?: (0L to 0L)

/**
 * Formats a [LocalDateTime] to a string of format "HH:mm".
 */
fun LocalDateTime.timeFormatted(): String = DateTimeFormatter.ofPattern("HH:mm").format(this)

/**
 * Formats a [LocalDateTime] to a string representing a full date in the format "EEEE, MMMM dd".
 */
fun LocalDateTime.dateFormatted(): String =
    DateTimeFormatter.ofPattern("EEEE, MMMM dd").format(this)

/**
 * Converts a [LocalDateTime] to milliseconds since the Unix epoch, assuming UTC timezone.
 */
fun LocalDateTime.utcMillis() = this
    .toEpochSecond(ZoneOffset.UTC)
    .times(1000)

/**
 * Converts a [LocalDateTime] to a [Date].
 */
fun LocalDateTime.toDate(): Date = Date.from(this.atZone(ZoneId.systemDefault()).toInstant())

/**
 * Converts a [Date] to a [LocalDateTime].
 */
fun Date.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault())

/**
 * Returns the start of the current week, adjusted to the first instance of Monday, as a [LocalDateTime].
 */
private fun firstInstantThisWeek() = firstInstantToday().with(DayOfWeek.MONDAY)

/**
 * Returns the end of the current week, adjusted to the first instance after Sunday, as a [LocalDateTime].
 */
private fun lastInstantThisWeek() = firstInstantToday().with(DayOfWeek.SUNDAY).plusDays(1)

/**
 * Returns the last moment of the current month as a [LocalDateTime].
 */
private fun lastInstantThisMonth() = with(firstInstantToday()) {
    withDayOfMonth(month.length(toLocalDate().isLeapYear))
}

/**
 * Checks if a [LocalDateTime] is between two other [LocalDateTime] instances.
 *
 * @param date The date to check.
 * @param after The start date for the range.
 * @param before The end date for the range.
 * @return True if [date] is after [after] and before [before].
 */
private fun isBetweenDates(date: LocalDateTime, after: LocalDateTime, before: LocalDateTime) =
    date.isAfter(after) && date.isBefore(before)
