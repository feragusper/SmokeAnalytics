package com.feragusper.smokeanalytics.features.goals.presentation.web

import com.feragusper.smokeanalytics.features.goals.domain.GoalBaselineKind
import com.feragusper.smokeanalytics.features.goals.domain.GoalCelebrationKind
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgressSpec
import com.feragusper.smokeanalytics.features.goals.domain.GoalSupportingSpec
import com.feragusper.smokeanalytics.features.goals.domain.GoalTargetSpec
import com.feragusper.smokeanalytics.features.goals.domain.GoalTitleKind
import com.feragusper.smokeanalytics.features.goals.domain.GoalWarningKind
import com.feragusper.smokeanalytics.libraries.design.i18n.AppStrings

internal fun GoalTitleKind.text(s: AppStrings): String = when (this) {
    GoalTitleKind.DailyCap -> s.goalTitleDailyCap
    GoalTitleKind.ReductionWeek -> s.goalTitleReductionWeek
    GoalTitleKind.ReductionMonth -> s.goalTitleReductionMonth
    GoalTitleKind.MindfulGap -> s.goalTitleMindfulGap
}

internal fun GoalTargetSpec.text(s: AppStrings): String = when (this) {
    is GoalTargetSpec.DailyCap -> s.goalTargetDailyCap(max)
    is GoalTargetSpec.ReduceByPercent -> s.goalTargetReduceByPercent(percentLabel)
    is GoalTargetSpec.SmokesOrFewer -> s.goalTargetSmokesOrFewer(countLabel)
    is GoalTargetSpec.WaitBetween -> s.goalTargetWaitBetween(durationLabel)
}

internal fun GoalProgressSpec.text(s: AppStrings): String = when (this) {
    is GoalProgressSpec.DailyCap -> s.goalProgressDailyCap(today, max)
    GoalProgressSpec.WaitingBaseline -> s.goalProgressWaitingBaseline
    is GoalProgressSpec.CurrentVsBaseline -> s.goalProgressCurrentVsBaseline(current, baseline)
    is GoalProgressSpec.CurrentGap -> s.goalProgressCurrentGap(durationLabel)
}

internal fun GoalBaselineKind.text(s: AppStrings): String = when (this) {
    GoalBaselineKind.PreviousWeek -> s.goalBaselinePreviousWeek
    GoalBaselineKind.PreviousMonth -> s.goalBaselinePreviousMonth
}

/** Returns null for [GoalSupportingSpec.None] so callers can skip rendering. */
internal fun GoalSupportingSpec.text(s: AppStrings): String? = when (this) {
    GoalSupportingSpec.None -> null
    is GoalSupportingSpec.CapRemaining -> s.goalSupportingCapRemaining(remaining)
    GoalSupportingSpec.CapOneMoreBreaks -> s.goalSupportingCapOneMoreBreaks
    GoalSupportingSpec.CapReachedHold -> s.goalSupportingCapReachedHold
    GoalSupportingSpec.CapExceeded -> s.goalSupportingCapExceeded
    GoalSupportingSpec.CapYesterdayUnder -> s.goalSupportingCapYesterdayUnder
    GoalSupportingSpec.ReduceBelowTarget -> s.goalSupportingReduceBelowTarget
    GoalSupportingSpec.ReduceMovingRight -> s.goalSupportingReduceMovingRight
    GoalSupportingSpec.ReduceStillAbove -> s.goalSupportingReduceStillAbove
    GoalSupportingSpec.ReduceNeedBaseline -> s.goalSupportingReduceNeedBaseline
    GoalSupportingSpec.GapMeetsTarget -> s.goalSupportingGapMeetsTarget
    GoalSupportingSpec.GapBuilding -> s.goalSupportingGapBuilding
    GoalSupportingSpec.GapStillShort -> s.goalSupportingGapStillShort
}

internal fun GoalWarningKind.text(s: AppStrings): String = when (this) {
    GoalWarningKind.OneMoreBreaksCap -> s.goalWarningOneMoreBreaksCap
    GoalWarningKind.CapBroken -> s.goalWarningCapBroken
}

internal fun GoalCelebrationKind.text(s: AppStrings): String = when (this) {
    GoalCelebrationKind.ReachedCapHold -> s.goalCelebrationReachedCapHold
    GoalCelebrationKind.YesterdayUnderCap -> s.goalCelebrationYesterdayUnderCap
}
