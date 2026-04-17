package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.goals.domain.GoalStatus
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

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
            "You need roughly 3h 20m between the remaining cigarettes to stay within today's cap.",
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
        assertEquals("Review your mindful gap in You", narrative.nextActionLabel)
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
        assertEquals("Today", readout.metrics[0].label)
        assertEquals("3 / 6", readout.metrics[0].value)
        assertEquals("Pace now", readout.metrics[1].label)
        assertEquals("2", readout.metrics[1].value)
        assertEquals("Margin", readout.metrics[2].label)
        assertEquals("3 left", readout.metrics[2].value)
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
}
