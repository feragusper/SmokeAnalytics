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
import kotlinx.datetime.toDeprecatedInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until

private val defaultTimeZone: TimeZone
    get() = TimeZone.currentSystemDefault()

private fun todayLocalDate(timeZone: TimeZone = defaultTimeZone): LocalDate =
    Clock.System.now().toLocalDateTime(timeZone).date

fun currentBucketDate(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
    manualDayStartEpochMillis: Long? = null,
): LocalDate = activeCurrentDayStartInstant(
    now = now,
    timeZone = timeZone,
    dayStartHour = dayStartHour,
    manualDayStartEpochMillis = manualDayStartEpochMillis,
).toLocalDateTime(timeZone).date

fun Instant.dayBucketDate(
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
    manualDayStartEpochMillis: Long? = null,
): LocalDate {
    val manualStart = manualDayStartEpochMillis?.let(Instant::fromEpochMilliseconds)
    if (manualStart != null && isInManualDayWindow(manualStart, timeZone, dayStartHour)) {
        return manualStart.toLocalDateTime(timeZone).date
    }
    val shifted = this.minus(dayStartHour, DateTimeUnit.HOUR, timeZone)
    return shifted.toLocalDateTime(timeZone).date
}

fun Instant.dayStartInstant(
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
): Instant {
    val date = dayBucketDate(timeZone = timeZone, dayStartHour = dayStartHour)
    return date.atStartOfDayIn(timeZone).plus(dayStartHour, DateTimeUnit.HOUR, timeZone).toDeprecatedInstant()
}

fun currentDayStartInstant(
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
    manualDayStartEpochMillis: Long? = null,
): Instant = activeCurrentDayStartInstant(
    timeZone = timeZone,
    dayStartHour = dayStartHour,
    manualDayStartEpochMillis = manualDayStartEpochMillis,
)

fun nextDayStartInstant(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
    manualDayStartEpochMillis: Long? = null,
): Instant = currentBucketDate(
    now = now,
    timeZone = timeZone,
    dayStartHour = dayStartHour,
    manualDayStartEpochMillis = manualDayStartEpochMillis,
).plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).plus(dayStartHour, DateTimeUnit.HOUR, timeZone).toDeprecatedInstant()

fun currentWeekStartInstant(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
    manualDayStartEpochMillis: Long? = null,
): Instant {
    val today = currentBucketDate(
        now = now,
        timeZone = timeZone,
        dayStartHour = dayStartHour,
        manualDayStartEpochMillis = manualDayStartEpochMillis,
    )
    val monday = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
    return monday.atStartOfDayIn(timeZone).plus(dayStartHour, DateTimeUnit.HOUR, timeZone).toDeprecatedInstant()
}

fun nextWeekStartInstant(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
    manualDayStartEpochMillis: Long? = null,
): Instant = currentWeekStartInstant(
    now = now,
    timeZone = timeZone,
    dayStartHour = dayStartHour,
    manualDayStartEpochMillis = manualDayStartEpochMillis,
).plus(7, DateTimeUnit.DAY, timeZone)

fun currentMonthStartInstant(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
    manualDayStartEpochMillis: Long? = null,
): Instant {
    val current = currentBucketDate(
        now = now,
        timeZone = timeZone,
        dayStartHour = dayStartHour,
        manualDayStartEpochMillis = manualDayStartEpochMillis,
    )
    return LocalDate(
        year = current.year,
        monthNumber = current.monthNumber,
        dayOfMonth = 1,
    ).atStartOfDayIn(timeZone).plus(dayStartHour, DateTimeUnit.HOUR, timeZone).toDeprecatedInstant()
}

fun nextMonthStartInstant(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
    manualDayStartEpochMillis: Long? = null,
): Instant {
    val current = currentBucketDate(
        now = now,
        timeZone = timeZone,
        dayStartHour = dayStartHour,
        manualDayStartEpochMillis = manualDayStartEpochMillis,
    )
    val nextMonth = LocalDate(
        year = if (current.monthNumber == 12) current.year + 1 else current.year,
        monthNumber = if (current.monthNumber == 12) 1 else current.monthNumber + 1,
        dayOfMonth = 1,
    )
    return nextMonth.atStartOfDayIn(timeZone).plus(dayStartHour, DateTimeUnit.HOUR, timeZone).toDeprecatedInstant()
}

fun lastInstantToday(timeZone: TimeZone = defaultTimeZone): Instant =
    todayLocalDate(timeZone).plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).toDeprecatedInstant()

fun firstInstantThisMonth(timeZone: TimeZone = defaultTimeZone): Instant {
    val today = todayLocalDate(timeZone)
    val firstDay = LocalDate(today.year, today.month, 1)
    return firstDay.atStartOfDayIn(timeZone).toDeprecatedInstant()
}

fun firstInstantThisMonth(
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
): Instant {
    val today = currentBucketDate(timeZone = timeZone, dayStartHour = dayStartHour)
    val firstDay = LocalDate(today.year, today.month, 1)
    return firstDay.atStartOfDayIn(timeZone).plus(dayStartHour, DateTimeUnit.HOUR, timeZone).toDeprecatedInstant()
}

fun Instant.isToday(timeZone: TimeZone = defaultTimeZone): Boolean =
    isBetweenInstants(
        instant = this,
        after = todayLocalDate(timeZone).atStartOfDayIn(timeZone).toDeprecatedInstant(),
        before = lastInstantToday(timeZone),
    )

fun Instant.isThisWeek(timeZone: TimeZone = defaultTimeZone): Boolean {
    val today = todayLocalDate(timeZone)
    val daysFromMonday = (today.dayOfWeek.isoDayNumber - 1)
    val monday = today.minus(daysFromMonday, DateTimeUnit.DAY)
    val start = monday.atStartOfDayIn(timeZone).toDeprecatedInstant()
    val end = monday.plus(7, DateTimeUnit.DAY).atStartOfDayIn(timeZone).toDeprecatedInstant()
    return isBetweenInstants(this, start, end)
}

fun Instant.isThisMonth(timeZone: TimeZone = defaultTimeZone): Boolean {
    val start = firstInstantThisMonth(timeZone)
    val today = todayLocalDate(timeZone)
    val nextMonthStart = LocalDate(
        year = if (today.monthNumber == 12) today.year + 1 else today.year,
        monthNumber = if (today.monthNumber == 12) 1 else today.monthNumber + 1,
        dayOfMonth = 1
    ).atStartOfDayIn(timeZone).toDeprecatedInstant()

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
    manualDayStartEpochMillis: Long? = null,
): Boolean {
    val start = currentDayStartInstant(
        timeZone = timeZone,
        dayStartHour = dayStartHour,
        manualDayStartEpochMillis = manualDayStartEpochMillis,
    )
    val end = nextDayStartInstant(
        timeZone = timeZone,
        dayStartHour = dayStartHour,
        manualDayStartEpochMillis = manualDayStartEpochMillis,
    )
    return isBetweenInstants(this, start, end)
}

fun Instant.isInCurrentWeekBucket(
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
    manualDayStartEpochMillis: Long? = null,
): Boolean {
    val today = currentBucketDate(
        timeZone = timeZone,
        dayStartHour = dayStartHour,
        manualDayStartEpochMillis = manualDayStartEpochMillis,
    )
    val monday = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
    val start = monday.atStartOfDayIn(timeZone).plus(dayStartHour, DateTimeUnit.HOUR, timeZone).toDeprecatedInstant()
    val end = start.plus(7, DateTimeUnit.DAY, timeZone)
    return isBetweenInstants(this, start, end)
}

fun Instant.isInCurrentMonthBucket(
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
    manualDayStartEpochMillis: Long? = null,
): Boolean {
    val start = firstInstantThisMonth(timeZone = timeZone, dayStartHour = dayStartHour)
    val current = currentBucketDate(
        timeZone = timeZone,
        dayStartHour = dayStartHour,
        manualDayStartEpochMillis = manualDayStartEpochMillis,
    )
    val nextMonth = LocalDate(
        year = if (current.monthNumber == 12) current.year + 1 else current.year,
        monthNumber = if (current.monthNumber == 12) 1 else current.monthNumber + 1,
        dayOfMonth = 1,
    )
    val end = nextMonth.atStartOfDayIn(timeZone).plus(dayStartHour, DateTimeUnit.HOUR, timeZone).toDeprecatedInstant()
    return isBetweenInstants(this, start, end)
}

fun shouldOfferStartNewDay(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
    manualDayStartEpochMillis: Long? = null,
    thresholdMinutes: Int = 120,
): Boolean {
    val manualStart = manualDayStartEpochMillis?.let(Instant::fromEpochMilliseconds)
    if (manualStart != null && now.isInManualDayWindow(manualStart, timeZone, dayStartHour)) return false
    val upcomingBoundary = now.dayStartInstant(timeZone = timeZone, dayStartHour = dayStartHour)
        .plus(1, DateTimeUnit.DAY, timeZone)
    val minutesToBoundary = now.until(upcomingBoundary, DateTimeUnit.MINUTE, timeZone).toInt()
    return minutesToBoundary in 0..thresholdMinutes
}

fun activeCurrentDayStartInstant(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = defaultTimeZone,
    dayStartHour: Int = 0,
    manualDayStartEpochMillis: Long? = null,
): Instant {
    val scheduledStart = now.dayStartInstant(timeZone = timeZone, dayStartHour = dayStartHour)
    val manualStart = manualDayStartEpochMillis?.let(Instant::fromEpochMilliseconds)
    return if (manualStart != null && now.isInManualDayWindow(manualStart, timeZone, dayStartHour)) {
        manualStart
    } else {
        scheduledStart
    }
}

private fun Instant.isInManualDayWindow(
    manualStart: Instant,
    timeZone: TimeZone,
    dayStartHour: Int,
): Boolean {
    val nextScheduledStart = manualStart.dayStartInstant(timeZone = timeZone, dayStartHour = dayStartHour)
        .plus(1, DateTimeUnit.DAY, timeZone)
    return this >= manualStart && this < nextScheduledStart
}
