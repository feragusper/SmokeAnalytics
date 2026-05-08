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

    @Test
    fun smokingGoalParserReturnsNullWhenTypeIsNull() {
        assertNull(smokingGoalOrNull(type = null as String?, metricValue = 15.0))
    }

    @Test
    fun smokingGoalParserReturnsNullWhenMetricIsNull() {
        assertNull(smokingGoalOrNull(type = "daily_cap", metricValue = null))
    }

    @Test
    fun goalTypeParserAcceptsWeekReductionAliases() {
        assertEquals(GoalType.ReductionVsPreviousWeek, goalTypeOrNull("ReductionVsPreviousWeek"))
        assertEquals(GoalType.ReductionVsPreviousWeek, goalTypeOrNull("weekly_reduction"))
        assertEquals(GoalType.ReductionVsPreviousWeek, goalTypeOrNull("week reduction"))
    }

    @Test
    fun goalTypeParserAcceptsMonthReductionAliases() {
        assertEquals(GoalType.ReductionVsPreviousMonth, goalTypeOrNull("ReductionVsPreviousMonth"))
        assertEquals(GoalType.ReductionVsPreviousMonth, goalTypeOrNull("monthly_reduction"))
        assertEquals(GoalType.ReductionVsPreviousMonth, goalTypeOrNull("month reduction"))
    }

    @Test
    fun goalTypeParserReturnsNullForNull() {
        assertNull(goalTypeOrNull(null))
    }

    @Test
    fun goalTypeParserReturnsNullForEmpty() {
        assertNull(goalTypeOrNull(""))
    }

    @Test
    fun smokingGoalBuildsReductionWeek() {
        val goal = smokingGoalOrNull(type = GoalType.ReductionVsPreviousWeek, metricValue = 20.0)
        assertIs<SmokingGoal.ReductionVsPreviousWeek>(goal)
        assertEquals(20.0, goal.reductionPercent)
    }

    @Test
    fun smokingGoalBuildsReductionMonth() {
        val goal = smokingGoalOrNull(type = GoalType.ReductionVsPreviousMonth, metricValue = 30.0)
        assertIs<SmokingGoal.ReductionVsPreviousMonth>(goal)
        assertEquals(30.0, goal.reductionPercent)
    }

    @Test
    fun smokingGoalBuildsMindfulGap() {
        val goal = smokingGoalOrNull(type = GoalType.MindfulGap, metricValue = 90.0)
        assertIs<SmokingGoal.MindfulGap>(goal)
        assertEquals(90, goal.targetMinutes)
    }

    @Test
    fun dailyCapCoercesMinimumTo1() {
        val goal = smokingGoalOrNull(type = GoalType.DailyCap, metricValue = 0.0)
        assertIs<SmokingGoal.DailyCap>(goal)
        assertEquals(1, goal.maxCigarettesPerDay)
    }

    @Test
    fun mindfulGapCoercesMinimumTo1() {
        val goal = smokingGoalOrNull(type = GoalType.MindfulGap, metricValue = 0.0)
        assertIs<SmokingGoal.MindfulGap>(goal)
        assertEquals(1, goal.targetMinutes)
    }

    @Test
    fun smokingGoalMetricValues() {
        assertEquals(10.0, SmokingGoal.DailyCap(10).metricValue)
        assertEquals(20.0, SmokingGoal.ReductionVsPreviousWeek(20.0).metricValue)
        assertEquals(30.0, SmokingGoal.ReductionVsPreviousMonth(30.0).metricValue)
        assertEquals(60.0, SmokingGoal.MindfulGap(60).metricValue)
    }

    @Test
    fun smokingGoalTypes() {
        assertEquals(GoalType.DailyCap, SmokingGoal.DailyCap(10).type)
        assertEquals(GoalType.ReductionVsPreviousWeek, SmokingGoal.ReductionVsPreviousWeek(20.0).type)
        assertEquals(GoalType.ReductionVsPreviousMonth, SmokingGoal.ReductionVsPreviousMonth(30.0).type)
        assertEquals(GoalType.MindfulGap, SmokingGoal.MindfulGap(60).type)
    }
}
