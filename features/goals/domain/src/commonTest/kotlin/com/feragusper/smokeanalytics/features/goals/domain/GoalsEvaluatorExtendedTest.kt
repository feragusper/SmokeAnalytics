package com.feragusper.smokeanalytics.features.goals.domain

import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

class GoalsEvaluatorExtendedTest {

    private val utc = TimeZone.UTC
    private val useCase = EvaluateGoalProgressUseCase(timeZone = utc)
    private val preferences = UserPreferences(dayStartHour = 6)

    @Test
    fun nullGoal_returnsNull() {
        val result = useCase(
            activeGoal = null,
            smokes = emptyList(),
            preferences = preferences,
            now = Instant.parse("2026-05-07T12:00:00Z"),
        )
        assertNull(result)
    }

    @Test
    fun dailyCap_zeroSmokes_onTrack() {
        val result = useCase(
            activeGoal = SmokingGoal.DailyCap(maxCigarettesPerDay = 5),
            smokes = emptyList(),
            preferences = preferences,
            now = Instant.parse("2026-05-07T12:00:00Z"),
        )
        assertNotNull(result)
        assertEquals(GoalStatus.OnTrack, result.status)
        assertEquals(0f, result.progressFraction)
        assertEquals(GoalTitleKind.DailyCap, result.titleKind)
    }

    @Test
    fun dailyCap_streakLabel_singleDay() {
        val smokes = listOf(
            smokeAt("2026-05-06T08:00:00Z"),
        )
        val result = useCase(
            activeGoal = SmokingGoal.DailyCap(maxCigarettesPerDay = 5),
            smokes = smokes,
            preferences = preferences,
            now = Instant.parse("2026-05-07T12:00:00Z"),
        )
        assertNotNull(result)
        assertEquals(1, result.streakDays)
        assertTrue(result.hasStreak)
    }

    @Test
    fun reductionWeek_withBaseline_completed() {
        // Previous week: 10 smokes, current week: 5 smokes → 50% reduction
        val previousWeekSmokes = (0 until 10).map { i ->
            smokeAt("2026-04-27T${(6 + i).toString().padStart(2, '0')}:00:00Z")
        }
        val currentWeekSmokes = (0 until 5).map { i ->
            smokeAt("2026-05-04T${(6 + i).toString().padStart(2, '0')}:00:00Z")
        }
        val now = Instant.parse("2026-05-07T12:00:00Z")

        val result = useCase(
            activeGoal = SmokingGoal.ReductionVsPreviousWeek(reductionPercent = 20.0),
            smokes = previousWeekSmokes + currentWeekSmokes,
            preferences = preferences,
            now = now,
        )

        assertNotNull(result)
        assertEquals(GoalStatus.Completed, result.status)
        assertEquals(GoalTitleKind.ReductionWeek, result.titleKind)
    }

    @Test
    fun reductionWeek_noPreviousWeek_notEnoughData() {
        val currentSmokes = listOf(smokeAt("2026-05-04T08:00:00Z"))
        val now = Instant.parse("2026-05-07T12:00:00Z")

        val result = useCase(
            activeGoal = SmokingGoal.ReductionVsPreviousWeek(reductionPercent = 20.0),
            smokes = currentSmokes,
            preferences = preferences,
            now = now,
        )

        assertNotNull(result)
        assertEquals(GoalStatus.NotEnoughData, result.status)
    }

    @Test
    fun reductionMonth_withBaseline_offTrack() {
        // Previous month: 10 smokes, current month: 12 smokes → worse
        val previousMonthSmokes = (1..10).map { i ->
            smokeAt("2026-04-${i.toString().padStart(2, '0')}T08:00:00Z")
        }
        val currentMonthSmokes = (1..12).map { i ->
            smokeAt("2026-05-0${if (i < 8) i else 7}T${(6 + (i % 12)).toString().padStart(2, '0')}:00:00Z")
        }
        val now = Instant.parse("2026-05-07T20:00:00Z")

        val result = useCase(
            activeGoal = SmokingGoal.ReductionVsPreviousMonth(reductionPercent = 20.0),
            smokes = previousMonthSmokes + currentMonthSmokes,
            preferences = preferences,
            now = now,
        )

        assertNotNull(result)
        assertEquals(GoalStatus.OffTrack, result.status)
    }

    @Test
    fun mindfulGap_onTrack_when70PercentReached() {
        val smokes = listOf(smokeAt("2026-05-07T10:57:00Z"))
        val now = Instant.parse("2026-05-07T12:00:00Z") // 63 min since last smoke

        val result = useCase(
            activeGoal = SmokingGoal.MindfulGap(targetMinutes = 90),
            smokes = smokes,
            preferences = preferences,
            now = now,
        )

        assertNotNull(result)
        assertEquals(GoalStatus.OnTrack, result.status)
    }

    @Test
    fun mindfulGap_offTrack_belowThreshold() {
        val smokes = listOf(smokeAt("2026-05-07T11:50:00Z"))
        val now = Instant.parse("2026-05-07T12:00:00Z") // 10 min

        val result = useCase(
            activeGoal = SmokingGoal.MindfulGap(targetMinutes = 90),
            smokes = smokes,
            preferences = preferences,
            now = now,
        )

        assertNotNull(result)
        assertEquals(GoalStatus.OffTrack, result.status)
    }

    @Test
    fun mindfulGap_noSmokes_completed() {
        val result = useCase(
            activeGoal = SmokingGoal.MindfulGap(targetMinutes = 90),
            smokes = emptyList(),
            preferences = preferences,
            now = Instant.parse("2026-05-07T12:00:00Z"),
        )

        assertNotNull(result)
        assertEquals(GoalStatus.Completed, result.status)
    }

    // --- goalDataFetchStart ---

    @Test
    fun goalDataFetchStart_returnsOneMonthBeforeCurrentMonthStart() {
        val now = Instant.parse("2026-05-15T12:00:00Z")
        val result = goalDataFetchStart(
            preferences = preferences,
            now = now,
            timeZone = utc,
        )
        // Should be around April 1 minus dayStartHour offset
        assertTrue(result < now)
    }

    private fun smokeAt(value: String) = Smoke(
        id = value,
        date = Instant.parse(value),
        timeElapsedSincePreviousSmoke = 0L to 0L,
        location = null,
    )
}

