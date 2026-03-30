package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeInsightsTest {

    @Test
    fun `rateSummary uses awake window to calculate the daily mindful gap target`() {
        val smokeCount = SmokeCountListResult(
            todaysSmokes = List(4) { index ->
                Smoke(
                    id = "$index",
                    date = Instant.parse("2026-03-25T0${index + 6}:00:00Z"),
                    timeElapsedSincePreviousSmoke = 2L to 0L,
                )
            },
            countByWeek = 16,
            countByMonth = 48,
            lastSmoke = Smoke(
                id = "last",
                date = Instant.parse("2026-03-25T10:00:00Z"),
                timeElapsedSincePreviousSmoke = 2L to 0L,
            ),
        )

        val summary = rateSummary(
            smokeCountListResult = smokeCount,
            preferences = UserPreferences(
                dayStartHour = 6,
                bedtimeHour = 22,
            ),
        )

        assertEquals(240, summary.averageIntervalMinutesToday)
        assertEquals(16.0 / 7.0, summary.averageSmokesPerDayWeek, 0.0001)
        assertEquals(1.6, summary.averageSmokesPerDayMonth, 0.0001)
    }

    @Test
    fun `toWidgetSnapshot keeps pulse-oriented metrics for the widget`() {
        val now = Clock.System.now()
        val smokeCount = SmokeCountListResult(
            todaysSmokes = List(4) { index ->
                Smoke(
                    id = "$index",
                    date = now,
                    timeElapsedSincePreviousSmoke = 2L to 0L,
                )
            },
            countByWeek = 14,
            countByMonth = 42,
            lastSmoke = Smoke(
                id = "last",
                date = now,
                timeElapsedSincePreviousSmoke = 2L to 0L,
            ),
        )

        val snapshot = smokeCount.toWidgetSnapshot(
            preferences = UserPreferences(
                dayStartHour = 6,
                bedtimeHour = 22,
            ),
        )

        assertEquals(4, snapshot.todayCount)
        assertTrue(snapshot.elapsedHours >= 0L)
        assertTrue(snapshot.elapsedMinutes >= 0L)
        assertEquals(240, snapshot.targetGapMinutes)
        assertEquals(2.0, snapshot.averageSmokesPerDayWeek, 0.0001)
    }
}
