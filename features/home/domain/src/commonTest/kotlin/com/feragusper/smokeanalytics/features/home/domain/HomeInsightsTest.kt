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
        assertEquals(GapPulseSpec.Until("25m", GapTargetKind.GoalGap), summary.pulseSummary)
        assertEquals(GapRecoverySpec.MindfulBuilding("2h"), summary.recoverySummary)
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
        assertEquals(GapPulseSpec.Until("3h 13m", GapTargetKind.DailyCapPace), summary.pulseSummary)
        assertEquals(GapRecoverySpec.DailyCapBuilding("3h 13m"), summary.recoverySummary)
    }

    @Test
    fun `gapFocusSummary with null elapsed shows log-a-smoke message`() {
        val summary = gapFocusSummary(
            elapsedMinutes = null,
            rateSummary = RateSummary(
                latestIntervalMinutes = 60,
                averageIntervalMinutesToday = 90,
                averageSmokesPerDayWeek = 10.0,
                averageSmokesPerDayMonth = 10.0,
            ),
            goalProgress = null,
        )
        assertEquals(90, summary.targetMinutes)
        assertEquals(null, summary.progressFraction)
        assertEquals(GapPulseSpec.LogOrRefresh, summary.pulseSummary)
    }

    @Test
    fun `gapFocusSummary with no goal and no rateSummary shows steady message`() {
        val summary = gapFocusSummary(
            elapsedMinutes = 45,
            rateSummary = RateSummary(
                latestIntervalMinutes = null,
                averageIntervalMinutesToday = 0,
                averageSmokesPerDayWeek = 0.0,
                averageSmokesPerDayMonth = 0.0,
            ),
            goalProgress = null,
        )
        assertEquals(null, summary.targetMinutes)
        assertEquals(null, summary.progressFraction)
        assertEquals(GapPulseSpec.StayWithGap, summary.pulseSummary)
        assertEquals(GapRecoverySpec.EachLongerGap, summary.recoverySummary)
    }

    @Test
    fun `gapFocusSummary elapsed past target shows 'beyond' message`() {
        val summary = gapFocusSummary(
            elapsedMinutes = 130,
            rateSummary = RateSummary(
                latestIntervalMinutes = 60,
                averageIntervalMinutesToday = 90,
                averageSmokesPerDayWeek = 10.0,
                averageSmokesPerDayMonth = 10.0,
            ),
            goalProgress = null,
        )
        assertEquals(90, summary.targetMinutes)
        assertEquals(1f, summary.progressFraction) // capped at 1
        assertEquals(GapPulseSpec.Beyond("40m", GapTargetKind.SteadyGap), summary.pulseSummary)
        assertEquals(GapRecoverySpec.SteadyBuilding("1h 30m"), summary.recoverySummary)
    }

    @Test
    fun `gapFocusSummary daily cap with all smoked has null target`() {
        val now = Instant.parse("2026-03-25T15:00:00Z")
        val summary = gapFocusSummary(
            elapsedMinutes = 30,
            rateSummary = null,
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 5),
                status = GoalStatus.Completed,
            ),
            smokesPerDay = 5,
            awakeMinutesPerDay = 960,
            dayStartHour = 6,
            bedtimeHour = 22,
            now = now,
            timeZone = utc,
        )
        assertEquals(null, summary.targetMinutes)
        assertEquals(null, summary.progressFraction)
    }

    @Test
    fun `rateSummary with zero daily smokes uses full awake window`() {
        val now = Instant.parse("2026-03-25T12:00:00Z")
        val smokeCount = SmokeCountListResult(
            todaysSmokes = emptyList(),
            countByWeek = 0,
            countByMonth = 0,
            lastSmoke = null,
        )
        val summary = rateSummary(
            smokeCountListResult = smokeCount,
            preferences = UserPreferences(dayStartHour = 6, bedtimeHour = 22),
            now = now,
            timeZone = utc,
        )
        assertEquals(960, summary.averageIntervalMinutesToday)
        assertEquals(null, summary.latestIntervalMinutes)
    }

    @Test
    fun `gamificationSummary with 8h and 12h gaps earns respective badges`() {
        val smokes = listOf(
            Smoke(id = "1", date = Instant.parse("2026-05-07T12:00:00Z"), timeElapsedSincePreviousSmoke = 13L to 0L),
            Smoke(id = "2", date = Instant.parse("2026-05-06T23:00:00Z"), timeElapsedSincePreviousSmoke = 9L to 0L),
        )
        val summary = gamificationSummary(smokes)
        assertEquals(13, summary.longestStreakHours)
        assertTrue(summary.badges.contains("8h"))
        assertTrue(summary.badges.contains("12h"))
    }

    @Test
    fun `gamificationSummary nextMilestone picks the right target`() {
        val smokes = listOf(
            Smoke(id = "1", date = Instant.parse("2026-05-07T12:00:00Z"), timeElapsedSincePreviousSmoke = 5L to 0L),
        )
        val summary = gamificationSummary(smokes)
        assertEquals(8, summary.nextMilestoneHours) // current is 5h → next milestone is 8
    }

    @Test
    fun `toWidgetSnapshot with no goal uses rateSummary target`() {
        val now = Instant.parse("2026-03-25T12:00:00Z")
        val smokeCount = SmokeCountListResult(
            todaysSmokes = List(3) { index ->
                Smoke(
                    id = "$index",
                    date = now,
                    timeElapsedSincePreviousSmoke = 2L to 0L,
                )
            },
            countByWeek = 12,
            countByMonth = 40,
            lastSmoke = Smoke(
                id = "last",
                date = now,
                timeElapsedSincePreviousSmoke = 2L to 0L,
            ),
        )
        val snapshot = smokeCount.toWidgetSnapshot(
            preferences = UserPreferences(dayStartHour = 6, bedtimeHour = 22),
            goalProgress = null,
            now = now,
            timeZone = utc,
        )
        assertEquals(3, snapshot.todayCount)
        assertEquals(320, snapshot.targetGapMinutes) // 960 / 3
    }
}
