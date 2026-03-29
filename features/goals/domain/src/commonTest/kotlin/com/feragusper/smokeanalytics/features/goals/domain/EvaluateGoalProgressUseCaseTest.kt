package com.feragusper.smokeanalytics.features.goals.domain

import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlin.test.Test
import kotlin.test.assertEquals
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
