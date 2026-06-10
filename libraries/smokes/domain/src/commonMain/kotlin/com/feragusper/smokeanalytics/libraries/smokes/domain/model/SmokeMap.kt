package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import com.feragusper.smokeanalytics.libraries.architecture.domain.currentDayStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentMonthStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentWeekStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextDayStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextMonthStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextWeekStartInstant
import kotlinx.datetime.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.pow
import kotlin.math.round
import kotlin.time.Clock

enum class SmokeMapPeriod {
    Day,
    Week,
    Month,
    Year,
}

data class SmokeMapCluster(
    val point: GeoPoint,
    val count: Int,
    val label: String,
    val radiusMeters: Int,
)

fun smokeMapRange(
    period: SmokeMapPeriod,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    dayStartHour: Int = 0,
    manualDayStartEpochMillis: Long? = null,
    now: Instant = Clock.System.now(),
    selectedDate: LocalDate? = null,
): Pair<Instant, Instant> {
    if (selectedDate != null) {
        return selectedDate.smokeMapRangeFor(period, timeZone, dayStartHour)
    }

    return when (period) {
        SmokeMapPeriod.Day -> currentDayStartInstant(
            now = now,
            timeZone = timeZone,
            dayStartHour = dayStartHour,
            manualDayStartEpochMillis = manualDayStartEpochMillis,
        ) to nextDayStartInstant(
            now = now,
            timeZone = timeZone,
            dayStartHour = dayStartHour,
            manualDayStartEpochMillis = manualDayStartEpochMillis,
        )

        SmokeMapPeriod.Week -> currentWeekStartInstant(
            now = now,
            timeZone = timeZone,
            dayStartHour = dayStartHour,
            manualDayStartEpochMillis = manualDayStartEpochMillis,
        ) to nextWeekStartInstant(
            now = now,
            timeZone = timeZone,
            dayStartHour = dayStartHour,
            manualDayStartEpochMillis = manualDayStartEpochMillis,
        )

        SmokeMapPeriod.Month -> currentMonthStartInstant(
            now = now,
            timeZone = timeZone,
            dayStartHour = dayStartHour,
            manualDayStartEpochMillis = manualDayStartEpochMillis,
        ) to nextMonthStartInstant(
            now = now,
            timeZone = timeZone,
            dayStartHour = dayStartHour,
            manualDayStartEpochMillis = manualDayStartEpochMillis,
        )

        SmokeMapPeriod.Year -> {
            val currentMonthStart = currentMonthStartInstant(
                now = now,
                timeZone = timeZone,
                dayStartHour = dayStartHour,
                manualDayStartEpochMillis = manualDayStartEpochMillis,
            )
            val current = currentMonthStart.minus(dayStartHour, DateTimeUnit.HOUR, timeZone)
                .toLocalDateTime(timeZone)
                .date
            LocalDate(current.year, 1, 1).atShiftedStartOfDay(timeZone, dayStartHour) to
                LocalDate(current.year + 1, 1, 1).atShiftedStartOfDay(timeZone, dayStartHour)
        }
    }
}

fun clusterSmokesForMap(
    smokes: List<Smoke>,
    period: SmokeMapPeriod,
): List<SmokeMapCluster> {
    val decimals = when (period) {
        SmokeMapPeriod.Day -> 3
        SmokeMapPeriod.Week -> 2
        SmokeMapPeriod.Month -> 1
        SmokeMapPeriod.Year -> 1
    }
    val radiusMeters = when (period) {
        SmokeMapPeriod.Day -> 120
        SmokeMapPeriod.Week -> 350
        SmokeMapPeriod.Month -> 900
        SmokeMapPeriod.Year -> 1200
    }

    return smokes
        .mapNotNull { it.location }
        .groupBy { point -> point.rounded(decimals) }
        .entries
        .sortedByDescending { it.value.size }
        .mapIndexed { index, (_, points) ->
            val centerLat = points.map { it.latitude }.average()
            val centerLon = points.map { it.longitude }.average()
            SmokeMapCluster(
                point = GeoPoint(centerLat, centerLon),
                count = points.size,
                label = when (index) {
                    0 -> "Top area"
                    1 -> "Second area"
                    2 -> "Third area"
                    else -> "Area ${index + 1}"
                },
                radiusMeters = radiusMeters,
            )
        }
}

private fun LocalDate.smokeMapRangeFor(
    period: SmokeMapPeriod,
    timeZone: TimeZone,
    dayStartHour: Int,
): Pair<Instant, Instant> = when (period) {
    SmokeMapPeriod.Day -> atShiftedStartOfDay(timeZone, dayStartHour) to
        plus(1, DateTimeUnit.DAY).atShiftedStartOfDay(timeZone, dayStartHour)

    SmokeMapPeriod.Week -> {
        val weekStart = minus(dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
        weekStart.atShiftedStartOfDay(timeZone, dayStartHour) to
            weekStart.plus(7, DateTimeUnit.DAY).atShiftedStartOfDay(timeZone, dayStartHour)
    }

    SmokeMapPeriod.Month -> {
        val monthStart = LocalDate(year, monthNumber, 1)
        monthStart.atShiftedStartOfDay(timeZone, dayStartHour) to
            monthStart.plus(1, DateTimeUnit.MONTH).atShiftedStartOfDay(timeZone, dayStartHour)
    }

    SmokeMapPeriod.Year -> LocalDate(year, 1, 1).atShiftedStartOfDay(timeZone, dayStartHour) to
        LocalDate(year + 1, 1, 1).atShiftedStartOfDay(timeZone, dayStartHour)
}

private fun LocalDate.atShiftedStartOfDay(
    timeZone: TimeZone,
    dayStartHour: Int,
): Instant = atStartOfDayIn(timeZone).plus(dayStartHour, DateTimeUnit.HOUR, timeZone)

private fun GeoPoint.rounded(decimals: Int): Pair<Double, Double> {
    val scale = 10.0.pow(decimals.toDouble())
    val roundedLat = round(latitude * scale) / scale
    val roundedLon = round(longitude * scale) / scale
    return roundedLat to roundedLon
}
