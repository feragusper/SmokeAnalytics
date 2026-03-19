package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import com.feragusper.smokeanalytics.libraries.architecture.domain.currentDayStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentMonthStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentWeekStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextDayStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextMonthStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextWeekStartInstant
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.math.pow
import kotlin.math.round

enum class SmokeMapPeriod {
    Day,
    Week,
    Month,
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
): Pair<Instant, Instant> = when (period) {
    SmokeMapPeriod.Day -> currentDayStartInstant(
        timeZone = timeZone,
        dayStartHour = dayStartHour,
        manualDayStartEpochMillis = manualDayStartEpochMillis,
    ) to nextDayStartInstant(
        timeZone = timeZone,
        dayStartHour = dayStartHour,
        manualDayStartEpochMillis = manualDayStartEpochMillis,
    )

    SmokeMapPeriod.Week -> currentWeekStartInstant(
        timeZone = timeZone,
        dayStartHour = dayStartHour,
        manualDayStartEpochMillis = manualDayStartEpochMillis,
    ) to nextWeekStartInstant(
        timeZone = timeZone,
        dayStartHour = dayStartHour,
        manualDayStartEpochMillis = manualDayStartEpochMillis,
    )

    SmokeMapPeriod.Month -> currentMonthStartInstant(
        timeZone = timeZone,
        dayStartHour = dayStartHour,
        manualDayStartEpochMillis = manualDayStartEpochMillis,
    ) to nextMonthStartInstant(
        timeZone = timeZone,
        dayStartHour = dayStartHour,
        manualDayStartEpochMillis = manualDayStartEpochMillis,
    )
}

fun clusterSmokesForMap(
    smokes: List<Smoke>,
    period: SmokeMapPeriod,
): List<SmokeMapCluster> {
    val decimals = when (period) {
        SmokeMapPeriod.Day -> 3
        SmokeMapPeriod.Week -> 2
        SmokeMapPeriod.Month -> 1
    }
    val radiusMeters = when (period) {
        SmokeMapPeriod.Day -> 120
        SmokeMapPeriod.Week -> 350
        SmokeMapPeriod.Month -> 900
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

private fun GeoPoint.rounded(decimals: Int): Pair<Double, Double> {
    val scale = 10.0.pow(decimals.toDouble())
    val roundedLat = round(latitude * scale) / scale
    val roundedLon = round(longitude * scale) / scale
    return roundedLat to roundedLon
}
