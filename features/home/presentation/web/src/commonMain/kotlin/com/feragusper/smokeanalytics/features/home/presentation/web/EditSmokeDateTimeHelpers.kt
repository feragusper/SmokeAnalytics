package com.feragusper.smokeanalytics.features.home.presentation.web

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

internal fun Instant.toDateInputValue(timeZone: TimeZone): String {
    val localDateTime = toLocalDateTime(timeZone)
    val year = localDateTime.year.toString().padStart(4, '0')
    val month = localDateTime.monthNumber.toString().padStart(2, '0')
    val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
    return "$year-$month-$day"
}

internal fun Instant.toTimeInputValue(timeZone: TimeZone): String {
    val localDateTime = toLocalDateTime(timeZone)
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

internal fun dateTimeInputsToInstant(
    dateValue: String,
    timeValue: String,
    timeZone: TimeZone,
): Instant {
    val year = dateValue.substring(0, 4).toInt()
    val month = dateValue.substring(5, 7).toInt()
    val day = dateValue.substring(8, 10).toInt()
    val hour = timeValue.substring(0, 2).toInt()
    val minute = timeValue.substring(3, 5).toInt()

    val localDateTime = LocalDateTime(
        year = year,
        monthNumber = month,
        dayOfMonth = day,
        hour = hour,
        minute = minute,
        second = 0,
        nanosecond = 0,
    )

    return localDateTime.toInstant(timeZone)
}
