package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * One logged cigarette, flattened for SwiftUI. [epochMillis] is the timestamp; [minutesSincePrevious]
 * is the gap to the previous smoke (-1 when unknown), so History can show the same "time between"
 * the Android list does.
 */
data class SmokeItem(
    val id: String,
    val epochMillis: Long,
    val minutesSincePrevious: Long,
)

/** A day of the month and how many cigarettes were logged that day (for the calendar heatmap). */
data class DayCount(val day: Int, val count: Int)

/** Swift-facing entry point for the Archive screen: calendar month counts + a day's smokes. */
class HistoryFacade : KoinComponent {

    private val fetchSmokes: FetchSmokesUseCase by inject()
    private val deleteSmoke: DeleteSmokeUseCase by inject()
    private val addSmoke: AddSmokeUseCase by inject()
    private val editSmoke: EditSmokeUseCase by inject()

    private val timeZone get() = TimeZone.currentSystemDefault()

    /** Per-day cigarette counts for the given month, for the calendar grid. */
    @Throws(Throwable::class)
    suspend fun monthCounts(year: Int, month: Int): List<DayCount> {
        val start = LocalDate(year, month, 1).atStartOfDayIn(timeZone)
        val end = LocalDate(year, month, 1).plus(DatePeriod(months = 1)).atStartOfDayIn(timeZone)
        return fetchSmokes(start, end)
            .groupingBy { it.date.toLocalDateTime(timeZone).dayOfMonth }
            .eachCount()
            .map { DayCount(day = it.key, count = it.value) }
    }

    /** Smokes logged on a single day, newest first, with the gap since the previous one. */
    @Throws(Throwable::class)
    suspend fun smokesForDay(year: Int, month: Int, day: Int): List<SmokeItem> {
        val start = LocalDate(year, month, day).atStartOfDayIn(timeZone)
        val end = LocalDate(year, month, day).plus(DatePeriod(days = 1)).atStartOfDayIn(timeZone)
        return fetchSmokes(start, end).map { smoke ->
            val (hours, minutes) = smoke.timeElapsedSincePreviousSmoke
            SmokeItem(
                id = smoke.id,
                epochMillis = smoke.date.toEpochMilliseconds(),
                minutesSincePrevious = if (hours == 0L && minutes == 0L) -1L else hours * 60 + minutes,
            )
        }
    }

    @Throws(Throwable::class)
    suspend fun delete(id: String) {
        deleteSmoke(id)
    }

    /** Moves a logged cigarette to a new timestamp. */
    @Throws(Throwable::class)
    suspend fun editSmoke(id: String, epochMillis: Long) {
        editSmoke(id, Instant.fromEpochMilliseconds(epochMillis))
    }

    /** Logs a cigarette at an arbitrary time (e.g. a missed one, from the Archive). */
    @Throws(Throwable::class)
    suspend fun addSmokeAt(epochMillis: Long) {
        addSmoke(Instant.fromEpochMilliseconds(epochMillis))
    }
}
