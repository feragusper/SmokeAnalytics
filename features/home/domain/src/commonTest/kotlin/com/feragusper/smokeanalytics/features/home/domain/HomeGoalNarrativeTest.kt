package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.goals.domain.GoalStatus
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeGoalNarrativeTest {
    private val utc = TimeZone.UTC
    private val noon = Instant.parse("2026-04-16T12:00:00Z")

    @Test
    fun `daily cap narrative leads with remaining cigarettes`() {
        val narrative = homeGoalNarrative(
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 6),
                title = "Daily cap",
                targetLabel = "Target: at most 6 today",
                progressLabel = "3 / 6 smoked today",
                supportingText = "3 left before reaching today's cap.",
                status = GoalStatus.OnTrack,
            ),
            smokesPerDay = 3,
            timeSinceLastCigarette = 3L to 0L,
            awakeMinutesPerDay = 960,
            dayStartHour = 6,
            bedtimeHour = 22,
            now = noon,
            timeZone = utc,
        )

        assertEquals("3 cigarettes left today", narrative.heroTitle)
        assertEquals("On track", narrative.statusLabel)
        assertEquals("Still within today's cap.", narrative.consistencyLabel)
        assertEquals(
            "~3h 20m between the remaining cigarettes.",
            narrative.heroSupporting,
        )
    }

    @Test
    fun `mindful gap narrative explains the wait target`() {
        val narrative = homeGoalNarrative(
            goalProgress = GoalProgress(
                goal = SmokingGoal.MindfulGap(targetMinutes = 90),
                title = "Mindful gap",
                targetLabel = "Target: wait 1h 30m between cigarettes",
                progressLabel = "Current gap: 50m",
                supportingText = "The current gap is building toward the target interval.",
                status = GoalStatus.OnTrack,
            ),
            smokesPerDay = 2,
        )

        assertEquals("Wait 1h 30m before the next cigarette", narrative.heroTitle)
        assertEquals("On track", narrative.statusLabel)
    }

    @Test
    fun `daily cap hero progress stays full when current gap is on pace`() {
        val progress = homeHeroProgress(
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 15),
                title = "Daily cap",
                targetLabel = "Target: at most 15 today",
                progressLabel = "3 / 15 smoked today",
                supportingText = "On track",
                status = GoalStatus.OnTrack,
                progressFraction = 0.2f,
            ),
            smokesPerDay = 3,
            timeSinceLastCigarette = 1L to 10L,
            awakeMinutesPerDay = 780,
            dayStartHour = 9,
            bedtimeHour = 22,
            now = noon,
            timeZone = utc,
        )

        assertEquals(1f, progress.fraction)
        assertEquals(HomeHeroProgressTone.Green, progress.tone)
    }

    @Test
    fun `daily cap hero progress decreases when smoking faster than expected`() {
        val progress = homeHeroProgress(
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 15),
                title = "Daily cap",
                targetLabel = "Target: at most 15 today",
                progressLabel = "3 / 15 smoked today",
                supportingText = "On track",
                status = GoalStatus.OnTrack,
                progressFraction = 0.2f,
            ),
            smokesPerDay = 7,
            timeSinceLastCigarette = 0L to 20L,
            awakeMinutesPerDay = 780,
            dayStartHour = 9,
            bedtimeHour = 22,
            now = noon,
            timeZone = utc,
        )

        assertEquals(HomeHeroProgressTone.Yellow, progress.tone)
        assertEquals(((180f / 780f) * 15f) / 7f, progress.fraction)
    }

    @Test
    fun `daily cap hero readout explains criterion instead of raw percent`() {
        val readout = homeHeroReadout(
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 6),
                title = "Daily cap",
                targetLabel = "Target: at most 6 today",
                progressLabel = "3 / 6 smoked today",
                supportingText = "On track",
                status = GoalStatus.OnTrack,
                progressFraction = 0.5f,
            ),
            smokesPerDay = 3,
            timeSinceLastCigarette = 1L to 15L,
            awakeMinutesPerDay = 960,
            dayStartHour = 6,
            bedtimeHour = 22,
            now = noon,
            timeZone = utc,
        )

        assertEquals("Cap used today", readout.meterLabel)
        assertEquals("3 of 6", readout.meterValue)
        assertEquals("Pace", readout.metrics[0].label)
        assertEquals("2", readout.metrics[0].value)
        assertEquals(HomeHeroMetricIcon.Pace, readout.metrics[0].icon)
        assertEquals("Margin", readout.metrics[1].label)
        assertEquals("3 left", readout.metrics[1].value)
        assertEquals("Every", readout.metrics[2].label)
        assertEquals("~3h 20m", readout.metrics[2].value)
    }

    @Test
    fun `daily cap hero progress uses cumulative pace right after logging`() {
        val progress = homeHeroProgress(
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 15),
                title = "Daily cap",
                targetLabel = "Target: at most 15 today",
                progressLabel = "5 / 15 smoked today",
                supportingText = "On track",
                status = GoalStatus.OnTrack,
                progressFraction = 0.33f,
            ),
            smokesPerDay = 5,
            timeSinceLastCigarette = 0L to 0L,
            awakeMinutesPerDay = 780,
            dayStartHour = 9,
            bedtimeHour = 22,
            now = noon,
            timeZone = utc,
        )

        assertEquals(HomeHeroProgressTone.Yellow, progress.tone)
        assertEquals(((180f / 780f) * 15f) / 5f, progress.fraction)
    }

    @Test
    fun `daily cap hero progress turns red and measures deviation when over cap`() {
        val progress = homeHeroProgress(
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 15),
                title = "Daily cap",
                targetLabel = "Target: at most 15 today",
                progressLabel = "16 / 15 smoked today",
                supportingText = "Over cap",
                status = GoalStatus.OffTrack,
                progressFraction = 1f,
            ),
            smokesPerDay = 16,
            timeSinceLastCigarette = 0L to 10L,
            awakeMinutesPerDay = 960,
            dayStartHour = 6,
            bedtimeHour = 22,
            now = noon,
            timeZone = utc,
        )

        assertEquals(HomeHeroProgressTone.Red, progress.tone)
        assertEquals(0.08f, progress.fraction)
    }

    @Test
    fun `mindful gap hero readout shows current gap target and remaining time`() {
        val readout = homeHeroReadout(
            goalProgress = GoalProgress(
                goal = SmokingGoal.MindfulGap(targetMinutes = 90),
                title = "Mindful gap",
                targetLabel = "Target: wait 1h 30m between cigarettes",
                progressLabel = "Current gap: 50m",
                supportingText = "The current gap is building toward the target interval.",
                status = GoalStatus.OnTrack,
                progressFraction = 0.55f,
            ),
            smokesPerDay = 2,
            timeSinceLastCigarette = 0L to 50L,
            awakeMinutesPerDay = 960,
        )

        assertEquals("Gap built", readout.meterLabel)
        assertEquals("50m of 1h 30m", readout.meterValue)
        assertEquals("Current", readout.metrics[0].label)
        assertEquals("50m", readout.metrics[0].value)
        assertEquals("Target", readout.metrics[1].label)
        assertEquals("1h 30m", readout.metrics[1].value)
        assertEquals("Remaining", readout.metrics[2].label)
        assertEquals("40m", readout.metrics[2].value)
    }

    @Test
    fun `missing goal falls back to setup narrative`() {
        val narrative = homeGoalNarrative(
            goalProgress = null,
            smokesPerDay = null,
        )

        assertEquals("Set one goal for today", narrative.heroTitle)
        assertEquals("No active goal", narrative.statusLabel)
    }

    @Test
    fun `elapsed gap formatter keeps readable units`() {
        assertEquals("45m", (0L to 45L).toElapsedGapLabel())
        assertEquals("2h", (2L to 0L).toElapsedGapLabel())
        assertEquals("2h 35m", (2L to 35L).toElapsedGapLabel())
    }

    @Test
    fun `null gap pair returns dash`() {
        assertEquals("--", null.toElapsedGapLabel())
    }

    @Test
    fun `toHomeClockLabel formats with two-digit padding`() {
        val instant = Instant.parse("2026-05-07T09:05:00Z")
        assertEquals("09:05", instant.toHomeClockLabel(utc))
    }

    @Test
    fun `toHomeClockLabel midnight`() {
        val instant = Instant.parse("2026-05-07T00:00:00Z")
        assertEquals("00:00", instant.toHomeClockLabel(utc))
    }

    @Test
    fun `elapsedToneFromMinutes boundaries`() {
        assertEquals(ElapsedTone.Urgent, elapsedToneFromMinutes(0))
        assertEquals(ElapsedTone.Urgent, elapsedToneFromMinutes(44))
        assertEquals(ElapsedTone.Warning, elapsedToneFromMinutes(45))
        assertEquals(ElapsedTone.Caution, elapsedToneFromMinutes(90))
        assertEquals(ElapsedTone.Calm, elapsedToneFromMinutes(180))
    }

    @Test
    fun `elapsedToneFrom with hours and minutes`() {
        assertEquals(ElapsedTone.Urgent, elapsedToneFrom(0, 10))
        assertEquals(ElapsedTone.Warning, elapsedToneFrom(0, 50))
        assertEquals(ElapsedTone.Caution, elapsedToneFrom(1, 35))
        assertEquals(ElapsedTone.Calm, elapsedToneFrom(3, 10))
    }

    @Test
    fun `greetingState morning with zero smokes`() {
        val greeting = greetingStateFor(hourOfDay = 8, todayCount = 0, currentStreakHours = 0)
        assertEquals("Good morning", greeting.title)
        assertEquals("Keep the first one away.", greeting.message)
    }

    @Test
    fun `greetingState afternoon with few smokes`() {
        val greeting = greetingStateFor(hourOfDay = 14, todayCount = 2, currentStreakHours = 0)
        assertEquals("Good afternoon", greeting.title)
        assertEquals("You are holding the line.", greeting.message)
    }

    @Test
    fun `greetingState evening with many smokes`() {
        val greeting = greetingStateFor(hourOfDay = 21, todayCount = 5, currentStreakHours = 0)
        assertEquals("Good evening", greeting.title)
        assertEquals("One less still counts.", greeting.message)
    }

    @Test
    fun `greetingState long streak overrides message`() {
        val greeting = greetingStateFor(hourOfDay = 10, todayCount = 2, currentStreakHours = 10)
        assertEquals("Strong pace today.", greeting.message)
    }

    @Test
    fun `financialSummary calculates spending correctly`() {
        val prefs = com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences(
            packPrice = 10.0,
            cigarettesPerPack = 20,
            currencySymbol = "$",
        )
        val summary = financialSummary(
            todayCount = 5,
            weekCount = 30,
            monthCount = 100,
            preferences = prefs,
        )
        assertEquals(2.5, summary.spentToday)
        assertEquals(15.0, summary.spentWeek)
        assertEquals(50.0, summary.spentMonth)
        assertEquals("$", summary.currencySymbol)
    }

    @Test
    fun `gamificationSummary with empty smokes returns defaults`() {
        val summary = gamificationSummary(emptyList())
        assertEquals(0, summary.currentStreakHours)
        assertEquals(0, summary.longestStreakHours)
        assertEquals(0, summary.points)
        assertEquals(2, summary.nextMilestoneHours)
        assertEquals(emptyList(), summary.badges)
    }

    @Test
    fun `gamificationSummary with smokes calculates badges`() {
        val smokes = listOf(
            com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke(
                id = "1",
                date = Instant.parse("2026-05-07T12:00:00Z"),
                timeElapsedSincePreviousSmoke = 4L to 0L,
            ),
            com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke(
                id = "2",
                date = Instant.parse("2026-05-07T08:00:00Z"),
                timeElapsedSincePreviousSmoke = 2L to 30L,
            ),
        )
        val summary = gamificationSummary(smokes)
        assertEquals(4, summary.longestStreakHours)
        kotlin.test.assertTrue(summary.badges.contains("2h"))
        kotlin.test.assertTrue(summary.badges.contains("4h"))
    }

    @Test
    fun `activeDayWindow normal range`() {
        val window = activeDayWindow(
            dayStartHour = 6,
            bedtimeHour = 22,
            awakeMinutesPerDay = 960,
            now = noon,
            timeZone = utc,
        )
        assertEquals(360, window.elapsedMinutes)
        assertEquals(600, window.remainingMinutes)
    }

    @Test
    fun `activeDayWindow before day start returns zero elapsed`() {
        val window = activeDayWindow(
            dayStartHour = 6,
            bedtimeHour = 22,
            awakeMinutesPerDay = 960,
            now = Instant.parse("2026-04-16T04:00:00Z"),
            timeZone = utc,
        )
        assertEquals(0, window.elapsedMinutes)
        assertEquals(960, window.remainingMinutes)
    }

    @Test
    fun `activeDayWindow after bedtime returns full day elapsed`() {
        val window = activeDayWindow(
            dayStartHour = 6,
            bedtimeHour = 22,
            awakeMinutesPerDay = 960,
            now = Instant.parse("2026-04-16T23:00:00Z"),
            timeZone = utc,
        )
        assertEquals(960, window.elapsedMinutes)
        assertEquals(0, window.remainingMinutes)
    }

    @Test
    fun `reduction weekly narrative shows percent`() {
        val narrative = homeGoalNarrative(
            goalProgress = GoalProgress(
                goal = SmokingGoal.ReductionVsPreviousWeek(reductionPercent = 20.0),
                title = "Reduction vs previous week",
                targetLabel = "Target: reduce by 20%",
                progressLabel = "5 vs 10 baseline",
                supportingText = "Below target.",
                status = GoalStatus.Completed,
            ),
            smokesPerDay = 3,
        )
        assertEquals("Reduce by 20% this week", narrative.heroTitle)
        assertEquals("Goal met", narrative.statusLabel)
    }

    @Test
    fun `reduction monthly narrative shows percent`() {
        val narrative = homeGoalNarrative(
            goalProgress = GoalProgress(
                goal = SmokingGoal.ReductionVsPreviousMonth(reductionPercent = 15.0),
                title = "Reduction vs previous month",
                targetLabel = "Target: reduce by 15%",
                progressLabel = "20 vs 30 baseline",
                supportingText = "On pace.",
                status = GoalStatus.OnTrack,
            ),
            smokesPerDay = 3,
        )
        assertEquals("Reduce by 15% this month", narrative.heroTitle)
        assertEquals("On track", narrative.statusLabel)
    }

    @Test
    fun `hero readout no goal suggests setup`() {
        val readout = homeHeroReadout(
            goalProgress = null,
            smokesPerDay = 5,
            timeSinceLastCigarette = 1L to 30L,
            awakeMinutesPerDay = 960,
        )
        assertEquals(4, readout.metrics.size)
        assertEquals("Cap", readout.metrics[0].label)
        assertEquals("Set one", readout.metrics[0].value)
        assertEquals("Gap", readout.metrics[1].label)
        assertEquals("Reduce", readout.metrics[2].label)
        assertEquals("Start", readout.metrics[3].label)
    }

    @Test
    fun `hero progress no goal returns neutral tone`() {
        val progress = homeHeroProgress(
            goalProgress = null,
            smokesPerDay = 5,
            timeSinceLastCigarette = 1L to 30L,
            awakeMinutesPerDay = 960,
        )
        assertEquals(HomeHeroProgressTone.Neutral, progress.tone)
    }

    @Test
    fun `daily cap narrative shows over cap hero title`() {
        val narrative = homeGoalNarrative(
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 5),
                title = "Daily cap",
                targetLabel = "Target: at most 5 today",
                progressLabel = "7 / 5 smoked today",
                supportingText = "Over cap",
                status = GoalStatus.OffTrack,
            ),
            smokesPerDay = 7,
            timeSinceLastCigarette = 0L to 10L,
            awakeMinutesPerDay = 960,
            dayStartHour = 6,
            bedtimeHour = 22,
            now = noon,
            timeZone = utc,
        )
        assertEquals("2 over today's cap", narrative.heroTitle)
        assertEquals("Over today's cap. Hold here.", narrative.heroSupporting)
        assertEquals("At risk", narrative.statusLabel)
    }

    @Test
    fun `daily cap narrative shows cap reached`() {
        val narrative = homeGoalNarrative(
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 5),
                title = "Daily cap",
                targetLabel = "Target: at most 5 today",
                progressLabel = "5 / 5 smoked today",
                supportingText = "Cap reached",
                status = GoalStatus.Completed,
            ),
            smokesPerDay = 5,
            timeSinceLastCigarette = 0L to 10L,
            awakeMinutesPerDay = 960,
            dayStartHour = 6,
            bedtimeHour = 22,
            now = noon,
            timeZone = utc,
        )
        assertEquals("0 cigarettes left today", narrative.heroTitle)
        assertEquals("Cap reached. Hold here.", narrative.heroSupporting)
    }

    @Test
    fun `daily cap narrative with 1 remaining uses singular`() {
        val narrative = homeGoalNarrative(
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 5),
                title = "Daily cap",
                targetLabel = "Target: at most 5 today",
                progressLabel = "4 / 5 smoked today",
                supportingText = "1 left",
                status = GoalStatus.OnTrack,
            ),
            smokesPerDay = 4,
            timeSinceLastCigarette = 1L to 0L,
            awakeMinutesPerDay = 960,
            dayStartHour = 6,
            bedtimeHour = 22,
            now = noon,
            timeZone = utc,
        )
        assertEquals("1 cigarette left today", narrative.heroTitle)
    }

    @Test
    fun `mindful gap hero progress uses progressFraction when on track`() {
        val progress = homeHeroProgress(
            goalProgress = GoalProgress(
                goal = SmokingGoal.MindfulGap(targetMinutes = 90),
                title = "Mindful gap",
                targetLabel = "Target: wait 1h 30m",
                progressLabel = "50m",
                supportingText = "On track",
                status = GoalStatus.OnTrack,
                progressFraction = 0.55f,
            ),
            smokesPerDay = 2,
            timeSinceLastCigarette = 0L to 50L,
            awakeMinutesPerDay = 960,
        )
        assertEquals(HomeHeroProgressTone.Green, progress.tone)
        assertEquals(0.55f, progress.fraction)
    }

    @Test
    fun `mindful gap hero progress off track shows yellow`() {
        val progress = homeHeroProgress(
            goalProgress = GoalProgress(
                goal = SmokingGoal.MindfulGap(targetMinutes = 90),
                title = "Mindful gap",
                targetLabel = "Target: wait 1h 30m",
                progressLabel = "20m",
                supportingText = "Off track",
                status = GoalStatus.OffTrack,
                progressFraction = 0.22f,
            ),
            smokesPerDay = 5,
            timeSinceLastCigarette = 0L to 20L,
            awakeMinutesPerDay = 960,
        )
        assertEquals(HomeHeroProgressTone.Yellow, progress.tone)
    }

    @Test
    fun `mindful gap hero progress not enough data shows neutral`() {
        val progress = homeHeroProgress(
            goalProgress = GoalProgress(
                goal = SmokingGoal.MindfulGap(targetMinutes = 90),
                title = "Mindful gap",
                targetLabel = "Target: wait 1h 30m",
                progressLabel = "--",
                supportingText = "Not enough data",
                status = GoalStatus.NotEnoughData,
                progressFraction = null,
            ),
            smokesPerDay = 0,
            timeSinceLastCigarette = null,
            awakeMinutesPerDay = 960,
        )
        assertEquals(HomeHeroProgressTone.Neutral, progress.tone)
    }

    @Test
    fun `reduction readout weekly shows window metrics`() {
        val readout = homeHeroReadout(
            goalProgress = GoalProgress(
                goal = SmokingGoal.ReductionVsPreviousWeek(reductionPercent = 20.0),
                title = "Reduction vs previous week",
                targetLabel = "Target: reduce by 20%",
                progressLabel = "5 vs 10 baseline",
                supportingText = "Below target.",
                status = GoalStatus.Completed,
                progressFraction = 0.5f,
                baselineLabel = "10 last week",
            ),
            smokesPerDay = 3,
            timeSinceLastCigarette = 1L to 0L,
            awakeMinutesPerDay = 960,
        )
        assertEquals("Reduction progress", readout.meterLabel)
        assertEquals("This week", readout.metrics[0].value)
        assertEquals("Target", readout.metrics[2].label)
    }

    @Test
    fun `reduction readout monthly shows window metrics`() {
        val readout = homeHeroReadout(
            goalProgress = GoalProgress(
                goal = SmokingGoal.ReductionVsPreviousMonth(reductionPercent = 15.0),
                title = "Reduction vs previous month",
                targetLabel = "Target: reduce by 15%",
                progressLabel = "20 vs 30 baseline",
                supportingText = "On pace.",
                status = GoalStatus.OnTrack,
                progressFraction = 0.67f,
                baselineLabel = "30 last month",
            ),
            smokesPerDay = 3,
            timeSinceLastCigarette = 1L to 0L,
            awakeMinutesPerDay = 960,
        )
        assertEquals("This month", readout.metrics[0].value)
        assertEquals("Status", readout.metrics[3].label)
    }

    @Test
    fun `homeHeroDebugBlock daily cap shows debug lines`() {
        val debugBlock = homeHeroDebugBlock(
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 6),
                title = "Daily cap",
                targetLabel = "Target: at most 6 today",
                progressLabel = "3 / 6 smoked today",
                supportingText = "On track",
                status = GoalStatus.OnTrack,
                progressFraction = 0.5f,
            ),
            smokesPerDay = 3,
            timeSinceLastCigarette = 1L to 15L,
            awakeMinutesPerDay = 960,
            dayStartHour = 6,
            bedtimeHour = 22,
            now = noon,
            timeZone = utc,
        )
        assertEquals("Debug: goal pace", debugBlock?.title)
        assertTrue(debugBlock!!.lines.any { it.contains("dailyCap = 6") })
        assertTrue(debugBlock.lines.any { it.contains("smokedToday = 3") })
    }

    @Test
    fun `homeHeroDebugBlock over cap shows deviation line`() {
        val debugBlock = homeHeroDebugBlock(
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 5),
                title = "Daily cap",
                targetLabel = "Target: at most 5 today",
                progressLabel = "7 / 5 smoked today",
                supportingText = "Over cap",
                status = GoalStatus.OffTrack,
                progressFraction = 1f,
            ),
            smokesPerDay = 7,
            timeSinceLastCigarette = 0L to 10L,
            awakeMinutesPerDay = 960,
            dayStartHour = 6,
            bedtimeHour = 22,
            now = noon,
            timeZone = utc,
        )
        assertTrue(debugBlock!!.lines.any { it.contains("deviation") })
    }

    @Test
    fun `homeHeroDebugBlock mindful gap shows goalType`() {
        val debugBlock = homeHeroDebugBlock(
            goalProgress = GoalProgress(
                goal = SmokingGoal.MindfulGap(targetMinutes = 90),
                title = "Mindful gap",
                targetLabel = "Target: wait 1h 30m",
                progressLabel = "50m",
                supportingText = "On track",
                status = GoalStatus.OnTrack,
                progressFraction = 0.55f,
            ),
            smokesPerDay = 2,
            timeSinceLastCigarette = 0L to 50L,
            awakeMinutesPerDay = 960,
            dayStartHour = 6,
            bedtimeHour = 22,
            now = noon,
            timeZone = utc,
        )
        assertEquals("Debug: goal pace", debugBlock?.title)
        assertTrue(debugBlock!!.lines.any { it.contains("MindfulGap") })
    }

    @Test
    fun `homeHeroDebugBlock null goal returns null`() {
        val debugBlock = homeHeroDebugBlock(
            goalProgress = null,
            smokesPerDay = 2,
            timeSinceLastCigarette = 0L to 50L,
            awakeMinutesPerDay = 960,
            dayStartHour = 6,
            bedtimeHour = 22,
            now = noon,
            timeZone = utc,
        )
        assertEquals(null, debugBlock)
    }

    @Test
    fun `daily cap readout over cap shows correct margin metric`() {
        val readout = homeHeroReadout(
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 5),
                title = "Daily cap",
                targetLabel = "Target: at most 5 today",
                progressLabel = "7 / 5 smoked today",
                supportingText = "Over cap",
                status = GoalStatus.OffTrack,
                progressFraction = 1f,
            ),
            smokesPerDay = 7,
            timeSinceLastCigarette = 0L to 10L,
            awakeMinutesPerDay = 960,
            dayStartHour = 6,
            bedtimeHour = 22,
            now = noon,
            timeZone = utc,
        )
        assertEquals("2 over", readout.metrics[1].value)
        assertEquals("--", readout.metrics[2].value)
    }

    @Test
    fun `daily cap readout 0 left shows hold metric`() {
        val readout = homeHeroReadout(
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 5),
                title = "Daily cap",
                targetLabel = "Target: at most 5 today",
                progressLabel = "5 / 5 smoked today",
                supportingText = "Cap reached",
                status = GoalStatus.Completed,
                progressFraction = 1f,
            ),
            smokesPerDay = 5,
            timeSinceLastCigarette = 0L to 10L,
            awakeMinutesPerDay = 960,
            dayStartHour = 6,
            bedtimeHour = 22,
            now = noon,
            timeZone = utc,
        )
        assertEquals("0 left", readout.metrics[1].value)
    }

    @Test
    fun `mindful gap readout when gap already met shows Ready now`() {
        val readout = homeHeroReadout(
            goalProgress = GoalProgress(
                goal = SmokingGoal.MindfulGap(targetMinutes = 60),
                title = "Mindful gap",
                targetLabel = "Target: wait 1h",
                progressLabel = "Current gap: 1h 15m",
                supportingText = "Met!",
                status = GoalStatus.Completed,
                progressFraction = 1f,
            ),
            smokesPerDay = 3,
            timeSinceLastCigarette = 1L to 15L,
            awakeMinutesPerDay = 960,
        )
        assertEquals("Ready now", readout.metrics[2].value)
    }

    @Test
    fun `mindful gap readout with null elapsed shows fallback`() {
        val readout = homeHeroReadout(
            goalProgress = GoalProgress(
                goal = SmokingGoal.MindfulGap(targetMinutes = 90),
                title = "Mindful gap",
                targetLabel = "Target: wait 1h 30m",
                progressLabel = "Unknown",
                supportingText = "Not enough data",
                status = GoalStatus.NotEnoughData,
                progressFraction = null,
            ),
            smokesPerDay = 0,
            timeSinceLastCigarette = null,
            awakeMinutesPerDay = 960,
        )
        assertEquals("--", readout.metrics[0].value)
    }

    @Test
    fun `activeDayWindow invalid dayStartHour returns safe defaults`() {
        val window = activeDayWindow(
            dayStartHour = 25,
            bedtimeHour = 22,
            awakeMinutesPerDay = 960,
            now = noon,
            timeZone = utc,
        )
        assertEquals(0, window.elapsedMinutes)
        assertEquals(960, window.remainingMinutes)
    }

    @Test
    fun `activeDayWindow zero awakeMinutes returns safe defaults`() {
        val window = activeDayWindow(
            dayStartHour = 6,
            bedtimeHour = 22,
            awakeMinutesPerDay = 0,
            now = noon,
            timeZone = utc,
        )
        assertEquals(0, window.elapsedMinutes)
        assertEquals(0, window.remainingMinutes)
    }

    @Test
    fun `gapFocusDebugBlock shows target source and delta`() {
        val rate = RateSummary(
            latestIntervalMinutes = 50,
            averageIntervalMinutesToday = 90,
            averageSmokesPerDayWeek = 10.0,
            averageSmokesPerDayMonth = 10.0,
        )
        val gapFocus = GapFocusSummary(
            targetMinutes = 120,
            progressFraction = 0.75f,
            pulseSummaryText = "30m until you reach your goal gap.",
            recoverySummaryText = "Building toward 2h.",
        )
        val debugBlock = gapFocusDebugBlock(
            elapsedMinutes = 90,
            rateSummary = rate,
            goalProgress = GoalProgress(
                goal = SmokingGoal.MindfulGap(targetMinutes = 120),
                title = "Mindful gap",
                targetLabel = "Target: wait 2h",
                progressLabel = "Current gap: 1h 30m",
                supportingText = "Keep going.",
                status = GoalStatus.OnTrack,
            ),
            gapFocus = gapFocus,
        )
        assertEquals("Debug: current gap", debugBlock.title)
        assertTrue(debugBlock.lines.any { it.contains("mindful-gap-goal") })
        assertTrue(debugBlock.lines.any { it.contains("-30m") })
    }

    @Test
    fun `gapFocusDebugBlock with null elapsed shows null labels`() {
        val gapFocus = GapFocusSummary(
            targetMinutes = 90,
            progressFraction = null,
            pulseSummaryText = "Log a smoke.",
            recoverySummaryText = "Building.",
        )
        val debugBlock = gapFocusDebugBlock(
            elapsedMinutes = null,
            rateSummary = null,
            goalProgress = null,
            gapFocus = gapFocus,
        )
        assertTrue(debugBlock.lines.any { it.contains("elapsedGap = null") })
        assertTrue(debugBlock.lines.any { it.contains("targetSource = rateSummary") })
    }

    @Test
    fun `gapFocusDebugBlock daily cap goal shows target source`() {
        val gapFocus = GapFocusSummary(
            targetMinutes = 60,
            progressFraction = 0.5f,
            pulseSummaryText = "30m left.",
            recoverySummaryText = "Building.",
        )
        val debugBlock = gapFocusDebugBlock(
            elapsedMinutes = 30,
            rateSummary = null,
            goalProgress = GoalProgress(
                goal = SmokingGoal.DailyCap(maxCigarettesPerDay = 10),
                title = "Daily cap",
                targetLabel = "Target: at most 10 today",
                progressLabel = "5 / 10",
                supportingText = "On track",
                status = GoalStatus.OnTrack,
            ),
            gapFocus = gapFocus,
        )
        assertTrue(debugBlock.lines.any { it.contains("daily-cap-goal") })
    }
}
