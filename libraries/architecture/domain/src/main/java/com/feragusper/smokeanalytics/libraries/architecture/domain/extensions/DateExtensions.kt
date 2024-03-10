package com.feragusper.smokeanalytics.libraries.architecture.domain.extensions

import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private fun firstInstantToday() = Calendar.getInstance().apply {
    time = Date()
}.withTimeZeroed()

private fun lastInstantToday() = firstInstantToday().apply {
    add(Calendar.DATE, 1)
}

private fun firstInstantThisWeek() = firstInstantToday().apply {
    firstDayOfWeek = Calendar.MONDAY
    this[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
}

private fun lastInstantThisWeek() = firstInstantToday().apply {
    firstDayOfWeek = Calendar.MONDAY
    this[Calendar.DAY_OF_WEEK] = Calendar.SUNDAY
    add(Calendar.DATE, 1)
}

private fun firstInstantThisMonth() = firstInstantToday().apply {
    this[Calendar.DAY_OF_MONTH] = getActualMinimum(Calendar.DAY_OF_MONTH)
}

private fun lastInstantThisMonth() = firstInstantToday().apply {
    this[Calendar.DAY_OF_MONTH] = getActualMaximum(Calendar.DAY_OF_MONTH)
}

private fun isBetweenDates(date: Date, after: Date, before: Date) =
    date.after(after) && date.before(before)

fun Date.isToday() = isBetweenDates(
    this,
    firstInstantToday().time,
    lastInstantToday().time
)

fun Date.isThisWeek() = isBetweenDates(
    this,
    firstInstantThisWeek().time,
    lastInstantThisWeek().time
)

fun Date.isThisMonth() = isBetweenDates(
    this,
    firstInstantThisMonth().time,
    lastInstantThisMonth().time
)

fun Date?.timeElapsedSinceNow(): Pair<Long, Long> = Date().timeAfter(this)

fun Date.timeAfter(date: Date?): Pair<Long, Long> = date?.let { dateNotNull ->
    (time - dateNotNull.time).let { diff ->
        TimeUnit.MILLISECONDS.toHours(diff) % 24 to TimeUnit.MILLISECONDS.toMinutes(diff) % 60
    }
} ?: (0L to 0L)

fun Date.withTimeZeroed(): Date =
    Calendar.getInstance().apply { time = this@withTimeZeroed }.withTimeZeroed().time

private fun Calendar.withTimeZeroed() = apply {
    this[Calendar.HOUR_OF_DAY] = 0
    this[Calendar.MINUTE] = 0
    this[Calendar.SECOND] = 0
    this[Calendar.MILLISECOND] = 0
}

fun Date.timeFormatted(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(this)
fun Date.dateFormatted(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(this)

fun Date.utcMillis() = toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
    .atStartOfDay()?.toEpochSecond(ZoneOffset.UTC)
    ?.times(1000)