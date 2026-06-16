package com.feragusper.smokeanalytics.libraries.cravings.domain

import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class CravingWaitCalculatorTest {

    private val calculator = CravingWaitCalculator()
    private val now = Instant.parse("2026-06-16T12:00:00Z")

    // dayStartHour 6, bedtimeHour 22 -> 16 awake hours -> 960 awake minutes.
    private val preferences = UserPreferences(dayStartHour = 6, bedtimeHour = 22)

    @Test
    fun `mindful gap suggests waiting the remaining minutes`() {
        val lastSmoke = now - 20.minutes
        val advice = calculator(
            activeGoal = SmokingGoal.MindfulGap(targetMinutes = 60),
            lastSmokeAt = lastSmoke,
            preferences = preferences,
            now = now,
        )

        assertEquals(60, advice.recommendedGapMinutes)
        assertEquals(lastSmoke + 60.minutes, advice.nextAllowedAt)
        assertFalse(advice.canSmokeNow)
        assertEquals(40, advice.waitMinutes)
    }

    @Test
    fun `mindful gap already satisfied allows smoking now`() {
        val advice = calculator(
            activeGoal = SmokingGoal.MindfulGap(targetMinutes = 60),
            lastSmokeAt = now - 90.minutes,
            preferences = preferences,
            now = now,
        )

        assertTrue(advice.canSmokeNow)
        assertEquals(0, advice.waitMinutes)
    }

    @Test
    fun `daily cap spreads cigarettes across the awake window`() {
        // 960 awake minutes / 8 per day = 120 min gap.
        val advice = calculator(
            activeGoal = SmokingGoal.DailyCap(maxCigarettesPerDay = 8),
            lastSmokeAt = now - 30.minutes,
            preferences = preferences,
            now = now,
        )

        assertEquals(120, advice.recommendedGapMinutes)
        assertEquals(90, advice.waitMinutes)
        assertFalse(advice.canSmokeNow)
    }

    @Test
    fun `reduction goal does not pace individual cigarettes`() {
        val advice = calculator(
            activeGoal = SmokingGoal.ReductionVsPreviousWeek(reductionPercent = 20.0),
            lastSmokeAt = now - 1.minutes,
            preferences = preferences,
            now = now,
        )

        assertNull(advice.recommendedGapMinutes)
        assertNull(advice.nextAllowedAt)
        assertTrue(advice.canSmokeNow)
    }

    @Test
    fun `no goal allows smoking now`() {
        val advice = calculator(
            activeGoal = null,
            lastSmokeAt = now - 1.minutes,
            preferences = preferences,
            now = now,
        )

        assertTrue(advice.canSmokeNow)
        assertNull(advice.nextAllowedAt)
    }

    @Test
    fun `no previous smoke allows smoking now`() {
        val advice = calculator(
            activeGoal = SmokingGoal.MindfulGap(targetMinutes = 60),
            lastSmokeAt = null,
            preferences = preferences,
            now = now,
        )

        assertTrue(advice.canSmokeNow)
        assertNull(advice.nextAllowedAt)
        assertEquals(60, advice.recommendedGapMinutes)
    }

    @Test
    fun `wait minutes round up so countdown never undershoots`() {
        val advice = calculator(
            activeGoal = SmokingGoal.MindfulGap(targetMinutes = 60),
            lastSmokeAt = now - 30.minutes - 30.minutes.div(60), // 30m30s ago -> 29m30s remaining
            preferences = preferences,
            now = now,
        )

        // 29m30s remaining rounds up to 30.
        assertEquals(30, advice.waitMinutes)
    }
}
