package com.feragusper.smokeanalytics.libraries.preferences.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class SmokingGoalTest {

    @Test
    fun goalTypeParserAcceptsCanonicalAndLegacyDailyCapNames() {
        assertEquals(GoalType.DailyCap, goalTypeOrNull("DailyCap"))
        assertEquals(GoalType.DailyCap, goalTypeOrNull("daily_cap"))
        assertEquals(GoalType.DailyCap, goalTypeOrNull("daily cap goal"))
    }

    @Test
    fun goalTypeParserAcceptsGapAliases() {
        assertEquals(GoalType.MindfulGap, goalTypeOrNull("MindfulGap"))
        assertEquals(GoalType.MindfulGap, goalTypeOrNull("steady_gap"))
    }

    @Test
    fun smokingGoalParserBuildsGoalFromRawType() {
        val goal = smokingGoalOrNull(type = "daily_cap", metricValue = 15.0)

        assertIs<SmokingGoal.DailyCap>(goal)
        assertEquals(15, goal.maxCigarettesPerDay)
    }

    @Test
    fun smokingGoalParserReturnsNullForUnknownType() {
        assertNull(smokingGoalOrNull(type = "something_else", metricValue = 15.0))
    }
}
