package com.feragusper.smokeanalytics.features.goals.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.feragusper.smokeanalytics.features.goals.domain.GoalBaselineKind
import com.feragusper.smokeanalytics.features.goals.domain.GoalCelebrationKind
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgressSpec
import com.feragusper.smokeanalytics.features.goals.domain.GoalSupportingSpec
import com.feragusper.smokeanalytics.features.goals.domain.GoalTargetSpec
import com.feragusper.smokeanalytics.features.goals.domain.GoalTitleKind
import com.feragusper.smokeanalytics.features.goals.domain.GoalWarningKind

@Composable
internal fun GoalTitleKind.text(): String = when (this) {
    GoalTitleKind.DailyCap -> stringResource(R.string.goals_title_daily_cap)
    GoalTitleKind.ReductionWeek -> stringResource(R.string.goals_title_reduction_week)
    GoalTitleKind.ReductionMonth -> stringResource(R.string.goals_title_reduction_month)
    GoalTitleKind.MindfulGap -> stringResource(R.string.goals_title_mindful_gap)
}

@Composable
internal fun GoalTargetSpec.text(): String = when (this) {
    is GoalTargetSpec.DailyCap -> stringResource(R.string.goals_target_daily_cap, max)
    is GoalTargetSpec.ReduceByPercent -> stringResource(R.string.goals_target_reduce_percent, percentLabel)
    is GoalTargetSpec.SmokesOrFewer -> stringResource(R.string.goals_target_smokes_or_fewer, countLabel)
    is GoalTargetSpec.WaitBetween -> stringResource(R.string.goals_target_wait_between, durationLabel)
}

@Composable
internal fun GoalProgressSpec.text(): String = when (this) {
    is GoalProgressSpec.DailyCap -> stringResource(R.string.goals_progress_daily_cap, today, max)
    GoalProgressSpec.WaitingBaseline -> stringResource(R.string.goals_progress_waiting_baseline)
    is GoalProgressSpec.CurrentVsBaseline -> stringResource(R.string.goals_progress_current_vs_baseline, current, baseline)
    is GoalProgressSpec.CurrentGap -> stringResource(R.string.goals_progress_current_gap, durationLabel)
}

@Composable
internal fun GoalBaselineKind.text(): String = when (this) {
    GoalBaselineKind.PreviousWeek -> stringResource(R.string.goals_baseline_previous_week)
    GoalBaselineKind.PreviousMonth -> stringResource(R.string.goals_baseline_previous_month)
}

/** Returns null for [GoalSupportingSpec.None] so callers can skip rendering. */
@Composable
internal fun GoalSupportingSpec.textOrNull(): String? = when (this) {
    GoalSupportingSpec.None -> null
    is GoalSupportingSpec.CapRemaining -> stringResource(R.string.goals_supporting_cap_remaining, remaining)
    GoalSupportingSpec.CapOneMoreBreaks -> stringResource(R.string.goals_supporting_cap_one_more)
    GoalSupportingSpec.CapReachedHold -> stringResource(R.string.goals_supporting_cap_reached)
    GoalSupportingSpec.CapExceeded -> stringResource(R.string.goals_supporting_cap_exceeded)
    GoalSupportingSpec.CapYesterdayUnder -> stringResource(R.string.goals_supporting_cap_yesterday)
    GoalSupportingSpec.ReduceBelowTarget -> stringResource(R.string.goals_supporting_reduce_below)
    GoalSupportingSpec.ReduceMovingRight -> stringResource(R.string.goals_supporting_reduce_moving)
    GoalSupportingSpec.ReduceStillAbove -> stringResource(R.string.goals_supporting_reduce_above)
    GoalSupportingSpec.ReduceNeedBaseline -> stringResource(R.string.goals_supporting_reduce_baseline)
    GoalSupportingSpec.GapMeetsTarget -> stringResource(R.string.goals_supporting_gap_meets)
    GoalSupportingSpec.GapBuilding -> stringResource(R.string.goals_supporting_gap_building)
    GoalSupportingSpec.GapStillShort -> stringResource(R.string.goals_supporting_gap_short)
}

@Composable
internal fun GoalWarningKind.text(): String = when (this) {
    GoalWarningKind.OneMoreBreaksCap -> stringResource(R.string.goals_warning_one_more)
    GoalWarningKind.CapBroken -> stringResource(R.string.goals_warning_cap_broken)
}

@Composable
internal fun GoalCelebrationKind.text(): String = when (this) {
    GoalCelebrationKind.ReachedCapHold -> stringResource(R.string.goals_celebration_reached)
    GoalCelebrationKind.YesterdayUnderCap -> stringResource(R.string.goals_celebration_yesterday)
}

@Composable
internal fun goalStreakText(days: Int): String =
    pluralStringResource(R.plurals.goals_streak_days, days, days)
