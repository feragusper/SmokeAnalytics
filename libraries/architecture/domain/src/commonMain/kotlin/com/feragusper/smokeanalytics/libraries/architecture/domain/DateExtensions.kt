package com.feragusper.smokeanalytics.libraries.architecture.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until

private val defaultTimeZone: TimeZone
    get() = TimeZone.currentSystemDefault()

private fun todayLocalDate(timeZone: TimeZone = defaultTimeZone): LocalDate =
    Clock.System.now().toLocalDateTime(timeZone).date

fun currentBucketDate(
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
): LocalDate = Clock.System.now().dayBucketDate(timeZone = timeZone, dayStartHour = dayStartHour)

fun Instant.dayBucketDate(
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
): LocalDate {
    val shifted = this.minus(dayStartHour, DateTimeUnit.HOUR, timeZone)
    return shifted.toLocalDateTime(timeZone).date
}

fun Instant.dayStartInstant(
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
): Instant {
    val date = dayBucketDate(timeZone = timeZone, dayStartHour = dayStartHour)
    return date.atStartOfDayIn(timeZone).plus(dayStartHour, DateTimeUnit.HOUR, timeZone)
}

fun currentDayStartInstant(
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
): Instant = Clock.System.now().dayStartInstant(timeZone = timeZone, dayStartHour = dayStartHour)

fun nextDayStartInstant(
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
): Instant = currentDayStartInstant(timeZone = timeZone, dayStartHour = dayStartHour)
    .plus(1, DateTimeUnit.DAY, timeZone)

fun lastInstantToday(timeZone: TimeZone = defaultTimeZone): Instant =
    todayLocalDate(timeZone).plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

fun firstInstantThisMonth(timeZone: TimeZone = defaultTimeZone): Instant {
    val today = todayLocalDate(timeZone)
    val firstDay = LocalDate(today.year, today.month, 1)
    return firstDay.atStartOfDayIn(timeZone)
}

fun firstInstantThisMonth(
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
): Instant {
    val today = currentBucketDate(timeZone = timeZone, dayStartHour = dayStartHour)
    val firstDay = LocalDate(today.year, today.month, 1)
    return firstDay.atStartOfDayIn(timeZone).plus(dayStartHour, DateTimeUnit.HOUR, timeZone)
}

fun Instant.isToday(timeZone: TimeZone = defaultTimeZone): Boolean =
    isBetweenInstants(
        instant = this,
        after = todayLocalDate(timeZone).atStartOfDayIn(timeZone),
        before = lastInstantToday(timeZone),
    )

fun Instant.isThisWeek(timeZone: TimeZone = defaultTimeZone): Boolean {
    val today = todayLocalDate(timeZone)
    val daysFromMonday = (today.dayOfWeek.isoDayNumber - 1)
    val monday = today.minus(daysFromMonday, DateTimeUnit.DAY)
    val start = monday.atStartOfDayIn(timeZone)
    val end = monday.plus(7, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
    return isBetweenInstants(this, start, end)
}

fun Instant.isThisMonth(timeZone: TimeZone = defaultTimeZone): Boolean {
    val start = firstInstantThisMonth(timeZone)
    val today = todayLocalDate(timeZone)
    val nextMonthStart = LocalDate(
        year = if (today.monthNumber == 12) today.year + 1 else today.year,
        monthNumber = if (today.monthNumber == 12) 1 else today.monthNumber + 1,
        dayOfMonth = 1
    ).atStartOfDayIn(timeZone)

    return isBetweenInstants(this, start, nextMonthStart)
}

fun Instant?.timeElapsedSinceNow(
    timeZone: TimeZone = defaultTimeZone
): Pair<Long, Long> =
    Clock.System.now().timeAfter(this, timeZone)

fun Instant.timeAfter(
    other: Instant?,
    timeZone: TimeZone = defaultTimeZone
): Pair<Long, Long> {
    if (other == null) return 0L to 0L

    val diffMinutes = other.until(this, DateTimeUnit.MINUTE, timeZone)
    val hours = diffMinutes / 60
    val minutes = diffMinutes % 60
    return hours to minutes
}

fun Instant.utcMillis(): Long = toEpochMilliseconds()

private fun isBetweenInstants(instant: Instant, after: Instant, before: Instant): Boolean =
    instant >= after && instant < before

fun Instant.isInCurrentDayBucket(
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
): Boolean {
    val start = currentDayStartInstant(timeZone = timeZone, dayStartHour = dayStartHour)
    val end = nextDayStartInstant(timeZone = timeZone, dayStartHour = dayStartHour)
    return isBetweenInstants(this, start, end)
}

fun Instant.isInCurrentWeekBucket(
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
): Boolean {
    val today = currentBucketDate(timeZone = timeZone, dayStartHour = dayStartHour)
    val monday = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
    val start = monday.atStartOfDayIn(timeZone).plus(dayStartHour, DateTimeUnit.HOUR, timeZone)
    val end = start.plus(7, DateTimeUnit.DAY, timeZone)
    return isBetweenInstants(this, start, end)
}

fun Instant.isInCurrentMonthBucket(
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
): Boolean {
    val start = firstInstantThisMonth(timeZone = timeZone, dayStartHour = dayStartHour)
    val current = currentBucketDate(timeZone = timeZone, dayStartHour = dayStartHour)
    val nextMonth = LocalDate(
        year = if (current.monthNumber == 12) current.year + 1 else current.year,
        monthNumber = if (current.monthNumber == 12) 1 else current.monthNumber + 1,
        dayOfMonth = 1,
    )
    val end = nextMonth.atStartOfDayIn(timeZone).plus(dayStartHour, DateTimeUnit.HOUR, timeZone)
    return isBetweenInstants(this, start, end)
}
