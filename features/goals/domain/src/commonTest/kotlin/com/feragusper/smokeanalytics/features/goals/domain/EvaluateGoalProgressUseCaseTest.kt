package com.feragusper.smokeanalytics.features.goals.domain

import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

class EvaluateGoalProgressUseCaseTest {

    private val timeZone = TimeZone.UTC
    private val useCase = EvaluateGoalProgressUseCase(timeZone = timeZone)
    private val preferences = UserPreferences(dayStartHour = 6)

    @Test
    fun `daily cap completes when count reaches target`() {
        val smokes = listOf(
            smokeAt("2026-03-29T08:00:00Z"),
            smokeAt("2026-03-29T09:00:00Z"),
        )

        val progress = useCase(
            activeGoal = SmokingGoal.DailyCap(maxCigarettesPerDay = 2),
            smokes = smokes,
            preferences = preferences,
            now = Instant.parse("2026-03-29T10:00:00Z"),
        )

        assertEquals(GoalStatus.Completed, progress?.status)
        assertTrue(progress?.celebrationLabel?.contains("reached today's cap") == true)
    }

    @Test
    fun `daily cap warns when one smoke away from breaking`() {
        val smokes = listOf(smokeAt("2026-03-29T08:00:00Z"))

        val progress = useCase(
            activeGoal = SmokingGoal.DailyCap(maxCigarettesPerDay = 2),
            smokes = smokes,
            preferences = preferences,
            now = Instant.parse("2026-03-29T10:00:00Z"),
        )

        assertEquals(GoalStatus.OnTrack, progress?.status)
        assertEquals("One more cigarette breaks today's cap.", progress?.warningLabel)
    }

    @Test
    fun `daily cap acknowledges yesterday success and streak`() {
        val smokes = listOf(
            smokeAt("2026-03-27T08:00:00Z"),
            smokeAt("2026-03-28T08:00:00Z"),
            smokeAt("2026-03-28T09:00:00Z"),
        )

        val progress = useCase(
            activeGoal = SmokingGoal.DailyCap(maxCigarettesPerDay = 2),
            smokes = smokes,
            preferences = preferences,
            now = Instant.parse("2026-03-29T10:00:00Z"),
        )

        assertEquals(2, progress?.streakDays)
        assertTrue(progress?.celebrationLabel?.contains("Yesterday stayed under your cap") == true)
        assertTrue(progress?.streakLabel?.contains("2 days") == true)
    }

    @Test
    fun `daily cap marks broken once count exceeds target`() {
        val smokes = listOf(
            smokeAt("2026-03-29T08:00:00Z"),
            smokeAt("2026-03-29T09:00:00Z"),
            smokeAt("2026-03-29T10:00:00Z"),
        )

        val progress = useCase(
            activeGoal = SmokingGoal.DailyCap(maxCigarettesPerDay = 2),
            smokes = smokes,
            preferences = preferences,
            now = Instant.parse("2026-03-29T10:30:00Z"),
        )

        assertEquals(GoalStatus.OffTrack, progress?.status)
        assertTrue(progress?.isBroken == true)
        assertFalse(progress?.warningLabel.isNullOrBlank())
    }

    @Test
    fun `week reduction needs baseline when previous week is empty`() {
        val smokes = listOf(smokeAt("2026-03-29T08:00:00Z"))

        val progress = useCase(
            activeGoal = SmokingGoal.ReductionVsPreviousWeek(reductionPercent = 20.0),
            smokes = smokes,
            preferences = preferences,
            now = Instant.parse("2026-03-29T10:00:00Z"),
        )

        assertEquals(GoalStatus.NotEnoughData, progress?.status)
    }

    @Test
    fun `mindful gap completes when elapsed exceeds target`() {
        val smokes = listOf(smokeAt("2026-03-29T08:00:00Z"))

        val progress = useCase(
            activeGoal = SmokingGoal.MindfulGap(targetMinutes = 90),
            smokes = smokes,
            preferences = preferences,
            now = Instant.parse("2026-03-29T10:00:00Z"),
        )

        assertEquals(GoalStatus.Completed, progress?.status)
    }

    private fun smokeAt(value: String) = Smoke(
        id = value,
        date = Instant.parse(value),
        timeElapsedSincePreviousSmoke = 0L to 0L,
        location = null,
    )
}
