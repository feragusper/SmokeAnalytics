package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.goals.domain.GoalStatus
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeInsightsTest {

    private val utc = TimeZone.UTC

    @Test
    fun `rateSummary uses awake window to calculate the daily mindful gap target`() {
        val now = Instant.parse("2026-03-25T12:00:00Z")
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
            now = now,
            timeZone = utc,
        )

        assertEquals(240, summary.averageIntervalMinutesToday)
        assertEquals(16.0 / 3.0, summary.averageSmokesPerDayWeek, 0.0001)
        assertEquals(48.0 / 25.0, summary.averageSmokesPerDayMonth, 0.0001)
    }

    @Test
    fun `rateSummary uses elapsed days only at the start of week and month`() {
        val now = Instant.parse("2026-06-01T09:00:00Z")
        val smokeCount = SmokeCountListResult(
            todaysSmokes = List(3) { index ->
                Smoke(
                    id = "$index",
                    date = Instant.parse("2026-06-01T0${index + 6}:00:00Z"),
                    timeElapsedSincePreviousSmoke = 1L to 0L,
                )
            },
            countByWeek = 18,
            countByMonth = 18,
            lastSmoke = Smoke(
                id = "last",
                date = Instant.parse("2026-06-01T08:00:00Z"),
                timeElapsedSincePreviousSmoke = 1L to 0L,
            ),
        )

        val summary = rateSummary(
            smokeCountListResult = smokeCount,
            preferences = UserPreferences(
                dayStartHour = 6,
                bedtimeHour = 22,
            ),
            now = now,
            timeZone = utc,
        )

        assertEquals(18.0, summary.averageSmokesPerDayWeek, 0.0001)
        assertEquals(18.0, summary.averageSmokesPerDayMonth, 0.0001)
    }

    @Test
    fun `toWidgetSnapshot keeps pulse-oriented metrics for the widget`() {
        val now = Instant.parse("2026-03-25T12:00:00Z")
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
            now = now,
            timeZone = utc,
        )

        assertEquals(4, snapshot.todayCount)
        assertTrue(snapshot.elapsedHours >= 0L)
        assertTrue(snapshot.elapsedMinutes >= 0L)
        assertEquals(240, snapshot.targetGapMinutes)
        assertEquals(14.0 / 3.0, snapshot.averageSmokesPerDayWeek, 0.0001)
    }

    @Test
    fun `toWidgetSnapshot uses active daily cap pace for next smoke wait`() {
        val now = Instant.parse("2026-03-25T15:34:00Z")
        val smokeCount = SmokeCountListResult(
            todaysSmokes = List(13) { index ->
                Smoke(
                    id = "$index",
                    date = now,
                    timeElapsedSincePreviousSmoke = 1L to 0L,
                )
            },
            countByWeek = 21,
            countByMonth = 64,
            lastSmoke = Smoke(
                id = "last",
                date = now,
                timeElapsedSincePreviousSmoke = 1L to 0L,
            ),
        )
        val preferences = UserPreferences(
            dayStartHour = 9,
            bedtimeHour = 22,
            activeGoal = SmokingGoal.DailyCap(maxCigarettesPerDay = 15),
        )
        val goalProgress = GoalProgress(
            goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 15),
            title = "Daily cap",
            targetLabel = "Target: at most 15 today",
            progressLabel = "13 / 15 smoked today",
            supportingText = "2 left before reaching today's cap.",
            status = GoalStatus.OnTrack,
        )

        val snapshot = smokeCount.toWidgetSnapshot(
            preferences = preferences,
            goalProgress = goalProgress,
            now = now,
            timeZone = utc,
        )

        assertEquals(13, snapshot.todayCount)
        assertEquals(193, snapshot.targetGapMinutes)
    }

    @Test
    fun `gapFocusSummary uses mindful gap goal as the source of truth when active`() {
        val now = Instant.parse("2026-03-25T12:00:00Z")
        val summary = gapFocusSummary(
            elapsedMinutes = 95,
            rateSummary = RateSummary(
                latestIntervalMinutes = 70,
                averageIntervalMinutesToday = 180,
                averageSmokesPerDayWeek = 12.0,
                averageSmokesPerDayMonth = 13.0,
            ),
            goalProgress = GoalProgress(
                goal = SmokingGoal.MindfulGap(targetMinutes = 120),
                title = "Mindful gap",
                targetLabel = "Target: wait 2h",
                progressLabel = "Current gap: 1h 35m",
                supportingText = "Keep going.",
                status = GoalStatus.OnTrack,
            ),
            awakeMinutesPerDay = 780,
            dayStartHour = 9,
            bedtimeHour = 22,
            now = now,
            timeZone = utc,
        )

        assertEquals(120, summary.targetMinutes)
        assertEquals(95f / 120f, summary.progressFraction)
        assertEquals("25m until you reach your goal gap.", summary.pulseSummaryText)
        assertEquals("You are building toward your 2h mindful gap goal.", summary.recoverySummaryText)
    }

    @Test
    fun `gapFocusSummary uses daily cap pace when daily cap goal is active`() {
        val now = Instant.parse("2026-03-25T15:34:00Z")
        val summary = gapFocusSummary(
            elapsedMinutes = 0,
            rateSummary = RateSummary(
                latestIntervalMinutes = null,
                averageIntervalMinutesToday = 180,
                averageSmokesPerDayWeek = 14.0,
                averageSmokesPerDayMonth = 14.0,
            ),
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 15),
                title = "Daily cap",
                targetLabel = "Target: at most 15 today",
                progressLabel = "6 / 15 smoked today",
                supportingText = "9 left before reaching today's cap.",
                status = GoalStatus.OnTrack,
                progressFraction = 0.4f,
            ),
            smokesPerDay = 13,
            awakeMinutesPerDay = 780,
            dayStartHour = 9,
            bedtimeHour = 22,
            now = now,
            timeZone = utc,
        )

        assertEquals(193, summary.targetMinutes)
        assertEquals("3h 13m until you reach your daily cap pace.", summary.pulseSummaryText)
        assertEquals("You are building toward a 3h 13m pace that keeps today's cap intact.", summary.recoverySummaryText)
    }
}
