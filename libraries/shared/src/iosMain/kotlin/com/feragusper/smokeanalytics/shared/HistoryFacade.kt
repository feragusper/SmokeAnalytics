package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
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

/** Swift-facing entry point for the History screen: the current month's smokes + delete. */
class HistoryFacade : KoinComponent {

    private val fetchSmokes: FetchSmokesUseCase by inject()
    private val deleteSmoke: DeleteSmokeUseCase by inject()

    /** This month's smokes, newest first (the repository's default range). */
    @Throws(Throwable::class)
    suspend fun loadMonth(): List<SmokeItem> = fetchSmokes().map { smoke ->
        val (hours, minutes) = smoke.timeElapsedSincePreviousSmoke
        val gap = hours * 60 + minutes
        SmokeItem(
            id = smoke.id,
            epochMillis = smoke.date.toEpochMilliseconds(),
            minutesSincePrevious = if (hours == 0L && minutes == 0L) -1L else gap,
        )
    }

    @Throws(Throwable::class)
    suspend fun delete(id: String) {
        deleteSmoke(id)
    }
}
