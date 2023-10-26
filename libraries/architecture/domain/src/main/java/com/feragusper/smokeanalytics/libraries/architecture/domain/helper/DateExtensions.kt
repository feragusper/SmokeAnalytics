package com.feragusper.smokeanalytics.libraries.architecture.domain.helper

import java.util.Calendar
import java.util.Date

fun firstInstantToday(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = Date()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    return calendar
}

fun lastInstantToday(): Calendar {
    val calendar: Calendar = firstInstantToday()
    calendar.add(Calendar.DATE, 1)
    return calendar
}

fun firstInstantThisWeek(): Calendar {
    val calendar = firstInstantToday()
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    return calendar
}

fun lastInstantThisWeek(): Calendar {
    val calendar = firstInstantToday()
    calendar.firstDayOfWeek = Calendar.MONDAY
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    calendar.add(Calendar.DATE, 1)
    return calendar
}

fun firstInstantThisMonth(): Calendar {
    val calendar = firstInstantToday()
    calendar.set(Calendar.DAY_OF_WEEK, 1)
    return calendar
}

fun lastInstantThisMonth(): Calendar {
    val calendar = firstInstantToday()
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    return calendar
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