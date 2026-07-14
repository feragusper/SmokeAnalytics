package com.feragusper.smokeanalytics.features.goals.domain

/**
 * Structured, presentation-localized descriptors for [GoalProgress] text. The domain decides
 * which variant applies and supplies the numbers/durations; the presentation layer (mobile
 * `strings.xml`, web `AppStrings`) turns each descriptor into localized copy.
 */

/** Which goal the progress card is about. */
enum class GoalTitleKind { DailyCap, ReductionWeek, ReductionMonth, MindfulGap }

/** The target line. */
sealed interface GoalTargetSpec {
    data class DailyCap(val max: Int) : GoalTargetSpec
    data class ReduceByPercent(val percentLabel: String) : GoalTargetSpec
    data class SmokesOrFewer(val countLabel: String) : GoalTargetSpec
    data class WaitBetween(val durationLabel: String) : GoalTargetSpec
}

/** The progress line. */
sealed interface GoalProgressSpec {
    data class DailyCap(val today: Int, val max: Int) : GoalProgressSpec
    data object WaitingBaseline : GoalProgressSpec
    data class CurrentVsBaseline(val current: Int, val baseline: Int) : GoalProgressSpec
    data class CurrentGap(val durationLabel: String) : GoalProgressSpec
}

/** Which baseline period a reduction goal compares against. */
enum class GoalBaselineKind { PreviousWeek, PreviousMonth }

/** The supporting line; [None] renders nothing. */
sealed interface GoalSupportingSpec {
    data object None : GoalSupportingSpec
    data class CapRemaining(val remaining: Int) : GoalSupportingSpec
    data object CapOneMoreBreaks : GoalSupportingSpec
    data object CapReachedHold : GoalSupportingSpec
    data object CapExceeded : GoalSupportingSpec
    data object CapYesterdayUnder : GoalSupportingSpec
    data object ReduceBelowTarget : GoalSupportingSpec
    data object ReduceMovingRight : GoalSupportingSpec
    data object ReduceStillAbove : GoalSupportingSpec
    data object ReduceNeedBaseline : GoalSupportingSpec
    data object GapMeetsTarget : GoalSupportingSpec
    data object GapBuilding : GoalSupportingSpec
    data object GapStillShort : GoalSupportingSpec
}

/** A warning banner when the cap is near/over. */
enum class GoalWarningKind { OneMoreBreaksCap, CapBroken }

/** A celebration banner when the cap is respected. */
enum class GoalCelebrationKind { ReachedCapHold, YesterdayUnderCap }
