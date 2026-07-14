package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.features.goals.domain.GoalBaselineKind
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgressSpec
import com.feragusper.smokeanalytics.features.goals.domain.GoalStatus
import com.feragusper.smokeanalytics.features.goals.domain.GoalSupportingSpec
import com.feragusper.smokeanalytics.features.goals.domain.GoalTargetSpec
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.time.Clock

/** Localized by presentation; [NoActiveGoal] is the no-goal case, the rest map from [GoalStatus]. */
enum class HomeGoalStatusLabel { NoActiveGoal, OnTrack, AtRisk, GoalMet, NeedsBaseline }

/** Hero title variants; presentation supplies the localized words, domain supplies the numbers. */
sealed interface HeroTitleSpec {
    data object SetOneGoal : HeroTitleSpec
    data class CigarettesLeft(val remaining: Int) : HeroTitleSpec
    data class OverCap(val over: Int) : HeroTitleSpec
    data class WaitBeforeNext(val durationLabel: String) : HeroTitleSpec
    data class ReduceThisWeek(val percentLabel: String) : HeroTitleSpec
    data class ReduceThisMonth(val percentLabel: String) : HeroTitleSpec
}

/** Hero supporting-line variants; [Goal] defers to a goals-domain supporting descriptor. */
sealed interface HeroSupportingSpec {
    data object SetGoalHint : HeroSupportingSpec
    data object OverCapHold : HeroSupportingSpec
    data object CapReachedHold : HeroSupportingSpec
    data object InsidePace : HeroSupportingSpec
    data class BetweenRemaining(val gapLabel: String) : HeroSupportingSpec
    data object FasterThanPace : HeroSupportingSpec
    data class Goal(val spec: GoalSupportingSpec) : HeroSupportingSpec
}

/** Consistency line variants; [StreakDays] carries the numeric streak for the localized plural. */
sealed interface ConsistencySpec {
    data class StreakDays(val days: Int) : ConsistencySpec
    data object NoGoalHint : ConsistencySpec
    data object CapStillWithin : ConsistencySpec
    data object CapReachedHold : ConsistencySpec
    data object CapPauseSteady : ConsistencySpec
    data object CapWaitingData : ConsistencySpec
    data object GapBuildingRight : ConsistencySpec
    data object GapMeetsTarget : ConsistencySpec
    data object GapFewMore : ConsistencySpec
    data object GapWaitingData : ConsistencySpec
    data object ReduceMovingRight : ConsistencySpec
    data object ReduceBelowTarget : ConsistencySpec
    data object ReduceSteadierNeeded : ConsistencySpec
    data object ReduceNeedBaseline : ConsistencySpec
}

data class HomeGoalNarrative(
    val heroTitle: HeroTitleSpec,
    val heroSupporting: HeroSupportingSpec,
    val status: HomeGoalStatusLabel,
    val consistency: ConsistencySpec,
    val streakDays: Int,
)

enum class HomeHeroProgressTone {
    Green,
    Yellow,
    Red,
    Neutral,
}

data class HomeHeroProgress(
    val fraction: Float,
    val tone: HomeHeroProgressTone,
)

/** Keyed hero-metric label; presentation localizes. */
enum class HeroMetricLabel { Cap, Gap, Reduce, Start, Every, Pace, Current, Target, Remaining, Status, Window }

/** Hero-metric value: [Raw] is a language-neutral number/duration; [Goal*] defer to goals domain. */
sealed interface HeroMetricValue {
    data class Raw(val text: String) : HeroMetricValue
    data class Status(val label: HomeGoalStatusLabel) : HeroMetricValue
    data class GoalTarget(val spec: GoalTargetSpec) : HeroMetricValue
    data class GoalProgress(val spec: GoalProgressSpec) : HeroMetricValue
    data object SetOne : HeroMetricValue
    data object BuildOne : HeroMetricValue
    data object TrackIt : HeroMetricValue
    data object Today : HeroMetricValue
    data object ThisWeek : HeroMetricValue
    data object ThisMonth : HeroMetricValue
    data object ReadyNow : HeroMetricValue
}

/** Hero-metric supporting line; [Goal*] defer to goals-domain descriptors. */
sealed interface HeroMetricSupporting {
    data class GoalSupporting(val spec: GoalSupportingSpec) : HeroMetricSupporting
    data class GoalBaseline(val kind: GoalBaselineKind) : HeroMetricSupporting
    data object LimitTodaysTotal : HeroMetricSupporting
    data object StretchNextWait : HeroMetricSupporting
    data object CompareLastWeek : HeroMetricSupporting
    data object MakeHomeUseful : HeroMetricSupporting
    data object PerRemainingCigarette : HeroMetricSupporting
    data object CapAlreadyUsed : HeroMetricSupporting
    data object NoActiveGapLeft : HeroMetricSupporting
    data object IdealByNow : HeroMetricSupporting
    data object SinceLastCigarette : HeroMetricSupporting
    data object MindfulGapGoal : HeroMetricSupporting
    data object NeededToHitTarget : HeroMetricSupporting
    data object TargetGapMet : HeroMetricSupporting
    data object ReductionGoal : HeroMetricSupporting
    data object HowThisGapReads : HeroMetricSupporting
    data object CurrentRead : HeroMetricSupporting
}

/** Keyed hero meter label; presentation localizes. */
enum class HeroMeterLabel { CapUsedToday, GapBuilt, ReductionProgress, SmokedToday, SinceLast, SpentToday }

/** Hero meter value: [Raw] is language-neutral; [Goal*] defer to goals-domain descriptors. */
sealed interface HeroMeterValue {
    data class Raw(val text: String) : HeroMeterValue
    data class GoalTarget(val spec: GoalTargetSpec) : HeroMeterValue
    data class GoalProgress(val spec: GoalProgressSpec) : HeroMeterValue
}

data class HomeHeroMetric(
    val label: HeroMetricLabel,
    val value: HeroMetricValue,
    val supporting: HeroMetricSupporting? = null,
    val icon: HomeHeroMetricIcon = HomeHeroMetricIcon.Focus,
)

enum class HomeHeroMetricIcon {
    Focus,
    Pace,
    Margin,
    Gap,
    Clock,
    Trend,
    Target,
    Window,
}

data class HomeHeroReadout(
    val meterLabel: HeroMeterLabel? = null,
    val meterValue: HeroMeterValue? = null,
    val meterFraction: Float? = null,
    val metrics: List<HomeHeroMetric> = emptyList(),
)

data class HomeDebugBlock(
    val title: String,
    val lines: List<String>,
)

data class ActiveDayWindow(
    val elapsedMinutes: Int,
    val remainingMinutes: Int,
)

fun homeGoalNarrative(
    goalProgress: GoalProgress?,
    smokesPerDay: Int?,
    timeSinceLastCigarette: Pair<Long, Long>? = null,
    awakeMinutesPerDay: Int = 0,
    dayStartHour: Int = 0,
    bedtimeHour: Int = 0,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): HomeGoalNarrative {
    if (goalProgress == null) {
        return HomeGoalNarrative(
            heroTitle = HeroTitleSpec.SetOneGoal,
            heroSupporting = HeroSupportingSpec.SetGoalHint,
            status = HomeGoalStatusLabel.NoActiveGoal,
            consistency = ConsistencySpec.NoGoalHint,
            streakDays = 0,
        )
    }

    return when (val goal = goalProgress.goal) {
        is SmokingGoal.DailyCap -> {
            val smokedToday = smokesPerDay ?: 0
            val remaining = goal.maxCigarettesPerDay - smokedToday
            val dayWindow = activeDayWindow(
                dayStartHour = dayStartHour,
                bedtimeHour = bedtimeHour,
                awakeMinutesPerDay = awakeMinutesPerDay,
                now = now,
                timeZone = timeZone,
            )
            val dynamicGapMinutes = dailyCapRemainingGapMinutes(
                remainingCigarettes = remaining,
                remainingActiveMinutes = dayWindow.remainingMinutes,
            )
            val expectedSmokesByNow = expectedSmokesByNow(
                elapsedActiveMinutes = dayWindow.elapsedMinutes,
                awakeMinutesPerDay = awakeMinutesPerDay,
                dailyCap = goal.maxCigarettesPerDay,
            )
            val pacingMessage: HeroSupportingSpec = when {
                remaining < 0 -> HeroSupportingSpec.OverCapHold
                remaining == 0 -> HeroSupportingSpec.CapReachedHold
                smokedToday <= expectedSmokesByNow -> HeroSupportingSpec.InsidePace
                dynamicGapMinutes != null -> HeroSupportingSpec.BetweenRemaining(dynamicGapMinutes.toDurationLabel())
                else -> HeroSupportingSpec.FasterThanPace
            }
            HomeGoalNarrative(
                heroTitle = when {
                    remaining >= 0 -> HeroTitleSpec.CigarettesLeft(remaining)
                    else -> HeroTitleSpec.OverCap(remaining.absoluteValue)
                },
                heroSupporting = pacingMessage,
                status = goalProgress.status.toHomeStatusLabel(),
                consistency = goalProgress.streakConsistency() ?: when (goalProgress.status) {
                    GoalStatus.OnTrack -> ConsistencySpec.CapStillWithin
                    GoalStatus.Completed -> ConsistencySpec.CapReachedHold
                    GoalStatus.OffTrack -> ConsistencySpec.CapPauseSteady
                    GoalStatus.NotEnoughData -> ConsistencySpec.CapWaitingData
                },
                streakDays = goalProgress.streakDays,
            )
        }

        is SmokingGoal.MindfulGap -> HomeGoalNarrative(
            heroTitle = HeroTitleSpec.WaitBeforeNext(goal.targetMinutes.toDurationLabel()),
            heroSupporting = HeroSupportingSpec.Goal(goalProgress.supporting),
            status = goalProgress.status.toHomeStatusLabel(),
            consistency = goalProgress.streakConsistency() ?: when (goalProgress.status) {
                GoalStatus.OnTrack -> ConsistencySpec.GapBuildingRight
                GoalStatus.Completed -> ConsistencySpec.GapMeetsTarget
                GoalStatus.OffTrack -> ConsistencySpec.GapFewMore
                GoalStatus.NotEnoughData -> ConsistencySpec.GapWaitingData
            },
            streakDays = goalProgress.streakDays,
        )

        is SmokingGoal.ReductionVsPreviousWeek -> HomeGoalNarrative(
            heroTitle = HeroTitleSpec.ReduceThisWeek(goal.reductionPercent.toCompactPercent()),
            heroSupporting = HeroSupportingSpec.Goal(goalProgress.supporting),
            status = goalProgress.status.toHomeStatusLabel(),
            consistency = goalProgress.streakConsistency() ?: reductionConsistency(goalProgress),
            streakDays = goalProgress.streakDays,
        )

        is SmokingGoal.ReductionVsPreviousMonth -> HomeGoalNarrative(
            heroTitle = HeroTitleSpec.ReduceThisMonth(goal.reductionPercent.toCompactPercent()),
            heroSupporting = HeroSupportingSpec.Goal(goalProgress.supporting),
            status = goalProgress.status.toHomeStatusLabel(),
            consistency = goalProgress.streakConsistency() ?: reductionConsistency(goalProgress),
            streakDays = goalProgress.streakDays,
        )
    }
}

fun homeHeroProgress(
    goalProgress: GoalProgress?,
    smokesPerDay: Int?,
    timeSinceLastCigarette: Pair<Long, Long>?,
    awakeMinutesPerDay: Int,
    dayStartHour: Int = 0,
    bedtimeHour: Int = 0,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): HomeHeroProgress {
    val goal = goalProgress?.goal
    if (goal is SmokingGoal.DailyCap) {
        val smokedToday = smokesPerDay ?: 0
        val dayWindow = activeDayWindow(
            dayStartHour = dayStartHour,
            bedtimeHour = bedtimeHour,
            awakeMinutesPerDay = awakeMinutesPerDay,
            now = now,
            timeZone = timeZone,
        )
        val expectedSmokesByNow = expectedSmokesByNow(
            elapsedActiveMinutes = dayWindow.elapsedMinutes,
            awakeMinutesPerDay = awakeMinutesPerDay,
            dailyCap = goal.maxCigarettesPerDay,
        )

        return when {
            smokedToday > goal.maxCigarettesPerDay -> {
                val deviation = ((smokedToday - goal.maxCigarettesPerDay).toFloat() / goal.maxCigarettesPerDay.toFloat())
                    .coerceIn(0f, 1f)
                    .coerceAtLeast(0.08f)
                HomeHeroProgress(
                    fraction = deviation,
                    tone = HomeHeroProgressTone.Red,
                )
            }

            smokedToday <= expectedSmokesByNow -> HomeHeroProgress(
                fraction = 1f,
                tone = HomeHeroProgressTone.Green,
            )

            else -> HomeHeroProgress(
                fraction = (expectedSmokesByNow / smokedToday.toFloat()).coerceIn(0.08f, 1f),
                tone = HomeHeroProgressTone.Yellow,
            )
        }
    }

    return HomeHeroProgress(
        fraction = goalProgress?.progressFraction?.coerceIn(0.08f, 1f) ?: 0.16f,
        tone = when (goalProgress?.status) {
            GoalStatus.Completed, GoalStatus.OnTrack -> HomeHeroProgressTone.Green
            GoalStatus.OffTrack -> HomeHeroProgressTone.Yellow
            GoalStatus.NotEnoughData, null -> HomeHeroProgressTone.Neutral
        },
    )
}

/** Which metric the user wants emphasized as the Home hero meter. */
enum class HomeHeroChoice { Auto, CountToday, Streak, MoneyToday }

/** Maps the persisted preference key to a [HomeHeroChoice]; unknown values fall back to Auto. */
fun homeHeroChoiceFromKey(key: String): HomeHeroChoice = when (key) {
    "count" -> HomeHeroChoice.CountToday
    "streak" -> HomeHeroChoice.Streak
    "money" -> HomeHeroChoice.MoneyToday
    else -> HomeHeroChoice.Auto
}

/**
 * The Home hero readout. [choice] lets the user override the top meter with a plain metric
 * (today's count, current streak, money spent today); [HomeHeroChoice.Auto] keeps the
 * goal-aware default. The sub-metrics below the meter are unchanged.
 */
fun homeHeroReadout(
    goalProgress: GoalProgress?,
    smokesPerDay: Int?,
    timeSinceLastCigarette: Pair<Long, Long>?,
    awakeMinutesPerDay: Int,
    dayStartHour: Int = 0,
    bedtimeHour: Int = 0,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    choice: HomeHeroChoice = HomeHeroChoice.Auto,
    cigarettePrice: Double = 0.0,
    currencySymbol: String = "",
): HomeHeroReadout {
    val base = homeHeroReadoutBase(
        goalProgress = goalProgress,
        smokesPerDay = smokesPerDay,
        timeSinceLastCigarette = timeSinceLastCigarette,
        awakeMinutesPerDay = awakeMinutesPerDay,
        dayStartHour = dayStartHour,
        bedtimeHour = bedtimeHour,
        now = now,
        timeZone = timeZone,
    )
    if (choice == HomeHeroChoice.Auto) return base

    val count = smokesPerDay ?: 0
    val (meterLabel, meterValueText) = when (choice) {
        HomeHeroChoice.CountToday -> HeroMeterLabel.SmokedToday to count.toString()
        HomeHeroChoice.Streak -> HeroMeterLabel.SinceLast to (
            timeSinceLastCigarette?.let { (h, m) -> "${h}h ${m}m" } ?: "--"
            )
        HomeHeroChoice.MoneyToday -> HeroMeterLabel.SpentToday to formatHeroMoney(currencySymbol, count * cigarettePrice)
        HomeHeroChoice.Auto -> return base
    }
    return base.copy(meterLabel = meterLabel, meterValue = HeroMeterValue.Raw(meterValueText), meterFraction = null)
}

private fun formatHeroMoney(currencySymbol: String, amount: Double): String {
    val rounded = (amount * 100).roundToInt()
    val whole = rounded / 100
    val cents = (rounded % 100).let { if (it < 10) "0$it" else "$it" }
    return "$currencySymbol$whole.$cents"
}

private fun homeHeroReadoutBase(
    goalProgress: GoalProgress?,
    smokesPerDay: Int?,
    timeSinceLastCigarette: Pair<Long, Long>?,
    awakeMinutesPerDay: Int,
    dayStartHour: Int = 0,
    bedtimeHour: Int = 0,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): HomeHeroReadout {
    val goal = goalProgress?.goal
    if (goal == null) {
        return HomeHeroReadout(
            metrics = listOf(
                HomeHeroMetric(
                    label = HeroMetricLabel.Cap,
                    value = HeroMetricValue.SetOne,
                    supporting = HeroMetricSupporting.LimitTodaysTotal,
                    icon = HomeHeroMetricIcon.Target,
                ),
                HomeHeroMetric(
                    label = HeroMetricLabel.Gap,
                    value = HeroMetricValue.BuildOne,
                    supporting = HeroMetricSupporting.StretchNextWait,
                    icon = HomeHeroMetricIcon.Gap,
                ),
                HomeHeroMetric(
                    label = HeroMetricLabel.Reduce,
                    value = HeroMetricValue.TrackIt,
                    supporting = HeroMetricSupporting.CompareLastWeek,
                    icon = HomeHeroMetricIcon.Trend,
                ),
                HomeHeroMetric(
                    label = HeroMetricLabel.Start,
                    value = HeroMetricValue.Today,
                    supporting = HeroMetricSupporting.MakeHomeUseful,
                    icon = HomeHeroMetricIcon.Focus,
                ),
            ),
        )
    }

    return when (goal) {
        is SmokingGoal.DailyCap -> {
            val smokedToday = smokesPerDay ?: 0
            val remaining = goal.maxCigarettesPerDay - smokedToday
            val dayWindow = activeDayWindow(
                dayStartHour = dayStartHour,
                bedtimeHour = bedtimeHour,
                awakeMinutesPerDay = awakeMinutesPerDay,
                now = now,
                timeZone = timeZone,
            )
            val expectedByNow = expectedSmokesByNow(
                elapsedActiveMinutes = dayWindow.elapsedMinutes,
                awakeMinutesPerDay = awakeMinutesPerDay,
                dailyCap = goal.maxCigarettesPerDay,
            ).roundToInt().coerceIn(0, goal.maxCigarettesPerDay)
            val remainingGapMinutes = dailyCapRemainingGapMinutes(
                remainingCigarettes = remaining,
                remainingActiveMinutes = dayWindow.remainingMinutes,
            )
            HomeHeroReadout(
                meterLabel = HeroMeterLabel.CapUsedToday,
                meterValue = HeroMeterValue.Raw("$smokedToday of ${goal.maxCigarettesPerDay}"),
                meterFraction = (smokedToday.toFloat() / goal.maxCigarettesPerDay.toFloat()).coerceIn(0f, 1f),
                metrics = listOf(
                    HomeHeroMetric(
                        label = HeroMetricLabel.Every,
                        value = HeroMetricValue.Raw(remainingGapMinutes?.let { "~${it.toDurationLabel()}" } ?: "--"),
                        supporting = when {
                            remainingGapMinutes != null -> HeroMetricSupporting.PerRemainingCigarette
                            remaining == 0 -> HeroMetricSupporting.CapAlreadyUsed
                            else -> HeroMetricSupporting.NoActiveGapLeft
                        },
                        icon = HomeHeroMetricIcon.Gap,
                    ),
                    HomeHeroMetric(
                        label = HeroMetricLabel.Pace,
                        value = HeroMetricValue.Raw(expectedByNow.toString()),
                        supporting = HeroMetricSupporting.IdealByNow,
                        icon = HomeHeroMetricIcon.Pace,
                    ),
                ),
            )
        }

        is SmokingGoal.MindfulGap -> {
            val elapsedMinutes = timeSinceLastCigarette?.let { (hours, minutes) -> (hours * 60L + minutes).toInt() }
            val remainingMinutes = elapsedMinutes?.let { (goal.targetMinutes - it).coerceAtLeast(0) }
            HomeHeroReadout(
                meterLabel = HeroMeterLabel.GapBuilt,
                meterValue = elapsedMinutes?.let { HeroMeterValue.Raw("${it.toDurationLabel()} of ${goal.targetMinutes.toDurationLabel()}") }
                    ?: HeroMeterValue.GoalProgress(goalProgress.progress),
                meterFraction = elapsedMinutes?.let {
                    (it.toFloat() / goal.targetMinutes.toFloat()).coerceIn(0f, 1f)
                } ?: goalProgress.progressFraction?.coerceIn(0f, 1f),
                metrics = listOf(
                    HomeHeroMetric(
                        label = HeroMetricLabel.Current,
                        value = HeroMetricValue.Raw(elapsedMinutes?.toDurationLabel() ?: "--"),
                        supporting = HeroMetricSupporting.SinceLastCigarette,
                        icon = HomeHeroMetricIcon.Clock,
                    ),
                    HomeHeroMetric(
                        label = HeroMetricLabel.Target,
                        value = HeroMetricValue.Raw(goal.targetMinutes.toDurationLabel()),
                        supporting = HeroMetricSupporting.MindfulGapGoal,
                        icon = HomeHeroMetricIcon.Target,
                    ),
                    HomeHeroMetric(
                        label = HeroMetricLabel.Remaining,
                        value = remainingMinutes?.takeIf { it > 0 }?.toDurationLabel()
                            ?.let { HeroMetricValue.Raw(it) } ?: HeroMetricValue.ReadyNow,
                        supporting = if ((remainingMinutes ?: 0) > 0) HeroMetricSupporting.NeededToHitTarget else HeroMetricSupporting.TargetGapMet,
                        icon = HomeHeroMetricIcon.Margin,
                    ),
                    HomeHeroMetric(
                        label = HeroMetricLabel.Status,
                        value = HeroMetricValue.Status(goalProgress.status.toHomeStatusLabel()),
                        supporting = HeroMetricSupporting.HowThisGapReads,
                        icon = HomeHeroMetricIcon.Focus,
                    ),
                ),
            )
        }

        is SmokingGoal.ReductionVsPreviousWeek -> HomeHeroReadout(
            meterLabel = HeroMeterLabel.ReductionProgress,
            meterValue = HeroMeterValue.GoalTarget(goalProgress.target),
            meterFraction = goalProgress.progressFraction?.coerceIn(0f, 1f),
            metrics = listOf(
                HomeHeroMetric(
                    label = HeroMetricLabel.Window,
                    value = HeroMetricValue.ThisWeek,
                    supporting = goalProgress.baseline?.let { HeroMetricSupporting.GoalBaseline(it) },
                    icon = HomeHeroMetricIcon.Window,
                ),
                HomeHeroMetric(
                    label = HeroMetricLabel.Pace,
                    value = HeroMetricValue.GoalProgress(goalProgress.progress),
                    supporting = HeroMetricSupporting.GoalSupporting(goalProgress.supporting),
                    icon = HomeHeroMetricIcon.Trend,
                ),
                HomeHeroMetric(
                    label = HeroMetricLabel.Target,
                    value = HeroMetricValue.GoalTarget(goalProgress.target),
                    supporting = HeroMetricSupporting.ReductionGoal,
                    icon = HomeHeroMetricIcon.Target,
                ),
                HomeHeroMetric(
                    label = HeroMetricLabel.Status,
                    value = HeroMetricValue.Status(goalProgress.status.toHomeStatusLabel()),
                    supporting = HeroMetricSupporting.CurrentRead,
                    icon = HomeHeroMetricIcon.Focus,
                ),
            ),
        )

        is SmokingGoal.ReductionVsPreviousMonth -> HomeHeroReadout(
            meterLabel = HeroMeterLabel.ReductionProgress,
            meterValue = HeroMeterValue.GoalTarget(goalProgress.target),
            meterFraction = goalProgress.progressFraction?.coerceIn(0f, 1f),
            metrics = listOf(
                HomeHeroMetric(
                    label = HeroMetricLabel.Window,
                    value = HeroMetricValue.ThisMonth,
                    supporting = goalProgress.baseline?.let { HeroMetricSupporting.GoalBaseline(it) },
                    icon = HomeHeroMetricIcon.Window,
                ),
                HomeHeroMetric(
                    label = HeroMetricLabel.Pace,
                    value = HeroMetricValue.GoalProgress(goalProgress.progress),
                    supporting = HeroMetricSupporting.GoalSupporting(goalProgress.supporting),
                    icon = HomeHeroMetricIcon.Trend,
                ),
                HomeHeroMetric(
                    label = HeroMetricLabel.Target,
                    value = HeroMetricValue.GoalTarget(goalProgress.target),
                    supporting = HeroMetricSupporting.ReductionGoal,
                    icon = HomeHeroMetricIcon.Target,
                ),
                HomeHeroMetric(
                    label = HeroMetricLabel.Status,
                    value = HeroMetricValue.Status(goalProgress.status.toHomeStatusLabel()),
                    supporting = HeroMetricSupporting.CurrentRead,
                    icon = HomeHeroMetricIcon.Focus,
                ),
            ),
        )
    }
}

fun homeHeroDebugBlock(
    goalProgress: GoalProgress?,
    smokesPerDay: Int?,
    timeSinceLastCigarette: Pair<Long, Long>?,
    awakeMinutesPerDay: Int,
    dayStartHour: Int = 0,
    bedtimeHour: Int = 0,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): HomeDebugBlock? {
    val elapsedMinutes = timeSinceLastCigarette?.let { it.first * 60 + it.second }
    val heroProgress = homeHeroProgress(
        goalProgress = goalProgress,
        smokesPerDay = smokesPerDay,
        timeSinceLastCigarette = timeSinceLastCigarette,
        awakeMinutesPerDay = awakeMinutesPerDay,
        dayStartHour = dayStartHour,
        bedtimeHour = bedtimeHour,
        now = now,
        timeZone = timeZone,
    )

    return when (val goal = goalProgress?.goal) {
        is SmokingGoal.DailyCap -> {
            val smokedToday = smokesPerDay ?: 0
            val remaining = goal.maxCigarettesPerDay - smokedToday
            val dayWindow = activeDayWindow(
                dayStartHour = dayStartHour,
                bedtimeHour = bedtimeHour,
                awakeMinutesPerDay = awakeMinutesPerDay,
                now = now,
                timeZone = timeZone,
            )
            val expectedGapMinutes = (awakeMinutesPerDay / goal.maxCigarettesPerDay).coerceAtLeast(1)
            val expectedByNow = expectedSmokesByNow(
                elapsedActiveMinutes = dayWindow.elapsedMinutes,
                awakeMinutesPerDay = awakeMinutesPerDay,
                dailyCap = goal.maxCigarettesPerDay,
            )
            val remainingGapMinutes = dailyCapRemainingGapMinutes(
                remainingCigarettes = remaining,
                remainingActiveMinutes = dayWindow.remainingMinutes,
            )
            val overCapBy = (smokedToday - goal.maxCigarettesPerDay).coerceAtLeast(0)
            val rawDeviationFraction = if (overCapBy > 0) {
                overCapBy.toFloat() / goal.maxCigarettesPerDay.toFloat()
            } else {
                null
            }
            HomeDebugBlock(
                title = "Debug: goal pace",
                lines = buildList {
                    add("awakeMinutesPerDay = $awakeMinutesPerDay")
                    add("dailyCap = ${goal.maxCigarettesPerDay}")
                    add("baseExpectedGap = awakeMinutesPerDay / dailyCap = $awakeMinutesPerDay / ${goal.maxCigarettesPerDay} = ${expectedGapMinutes}m")
                    add("activeElapsedMinutes = ${dayWindow.elapsedMinutes}m")
                    add("activeRemainingMinutes = ${dayWindow.remainingMinutes}m")
                    add("elapsedGap = ${elapsedMinutes.toDebugMinutesLabel()}")
                    add("smokedToday = $smokedToday")
                    add("remaining = ${goal.maxCigarettesPerDay} - $smokedToday = $remaining")
                    add("expectedSmokesByNow = (activeElapsedMinutes / awakeMinutesPerDay) * dailyCap = (${dayWindow.elapsedMinutes} / $awakeMinutesPerDay) * ${goal.maxCigarettesPerDay} = ${expectedByNow.toInt()}")
                    add("remainingGapTarget = activeRemainingMinutes / remaining = ${dayWindow.remainingMinutes} / ${remaining.coerceAtLeast(1)} = ${remainingGapMinutes?.let { "${it}m" } ?: "null"}")
                    add("paceRatio = expectedSmokesByNow / smokedToday = ${expectedByNow.toInt()} / $smokedToday = ${if (smokedToday > 0) ((expectedByNow / smokedToday.toFloat()) * 100).toInt() else 100}%")
                    if (rawDeviationFraction != null) {
                        add("deviation = overCap / dailyCap = $overCapBy / ${goal.maxCigarettesPerDay} = ${(rawDeviationFraction * 100).toInt()}%")
                    }
                    add("heroFractionShown = ${(heroProgress.fraction * 100).toInt()}%")
                    add("heroTone = ${heroProgress.tone.name}")
                },
            )
        }

        is SmokingGoal.MindfulGap -> HomeDebugBlock(
            title = "Debug: goal pace",
            lines = buildList {
                add("goalType = MindfulGap")
                add("targetGap = ${goal.targetMinutes}m")
                add("elapsedGap = ${elapsedMinutes.toDebugMinutesLabel()}")
                add("goalProgressFractionShown = ${(heroProgress.fraction * 100).toInt()}%")
                add("heroTone = ${heroProgress.tone.name}")
            },
        )

        else -> null
    }
}

fun gapFocusDebugBlock(
    elapsedMinutes: Long?,
    rateSummary: RateSummary?,
    goalProgress: GoalProgress?,
    gapFocus: GapFocusSummary,
): HomeDebugBlock {
    val targetSource = when (goalProgress?.goal) {
        is SmokingGoal.MindfulGap -> "mindful-gap-goal"
        is SmokingGoal.DailyCap -> "daily-cap-goal"
        else -> "rateSummary.averageIntervalMinutesToday"
    }
    val deltaMinutes = when {
        elapsedMinutes == null || gapFocus.targetMinutes == null -> null
        else -> elapsedMinutes - gapFocus.targetMinutes.toLong()
    }
    return HomeDebugBlock(
        title = "Debug: current gap",
        lines = buildList {
            add("elapsedGap = ${elapsedMinutes.toDebugMinutesLabel()}")
            add("targetSource = $targetSource")
            add("targetGap = ${gapFocus.targetMinutes.toDebugMinutesLabel()}")
            add("rateSummary.averageIntervalMinutesToday = ${rateSummary?.averageIntervalMinutesToday.toDebugMinutesLabel()}")
            add("rateSummary.latestIntervalMinutes = ${rateSummary?.latestIntervalMinutes.toDebugMinutesLabel()}")
            if (elapsedMinutes != null && gapFocus.targetMinutes != null && gapFocus.targetMinutes > 0) {
                add("gapRatio = elapsedGap / targetGap = $elapsedMinutes / ${gapFocus.targetMinutes} = ${((elapsedMinutes.toFloat() / gapFocus.targetMinutes.toFloat()) * 100).toInt()}%")
            }
            add("progressShown = ${gapFocus.progressFraction?.toPercentLabel() ?: "null"}")
            add("delta = ${deltaMinutes.toSignedDebugMinutesLabel()}")
            add("summary = ${gapFocus.pulseSummary}")
        },
    )
}

fun Pair<Long, Long>?.toElapsedGapLabel(): String = this?.let { (hours, minutes) ->
    when {
        hours <= 0L -> "${minutes}m"
        minutes <= 0L -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
} ?: "--"

fun Instant.toHomeClockLabel(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    use24HourClock: Boolean = true,
): String {
    val time = toLocalDateTime(timeZone).time
    return formatClock(time.hour, time.minute, use24HourClock)
}

/** Shared clock formatter: "14:05" (24h) or "2:05 PM" (12h). */
fun formatClock(hour: Int, minute: Int, use24HourClock: Boolean): String {
    if (use24HourClock) return "${hour.toTwoDigits()}:${minute.toTwoDigits()}"
    val suffix = if (hour < 12) "AM" else "PM"
    val h12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$h12:${minute.toTwoDigits()} $suffix"
}

private fun reductionConsistency(goalProgress: GoalProgress): ConsistencySpec =
    when (goalProgress.status) {
        GoalStatus.OnTrack -> ConsistencySpec.ReduceMovingRight
        GoalStatus.Completed -> ConsistencySpec.ReduceBelowTarget
        GoalStatus.OffTrack -> ConsistencySpec.ReduceSteadierNeeded
        GoalStatus.NotEnoughData -> ConsistencySpec.ReduceNeedBaseline
    }

/** A streak line when one exists (streakLabel is non-null exactly when streakDays > 0). */
private fun GoalProgress.streakConsistency(): ConsistencySpec? =
    if (hasStreak) ConsistencySpec.StreakDays(streakDays) else null

private fun GoalStatus.toHomeStatusLabel(): HomeGoalStatusLabel = when (this) {
    GoalStatus.OnTrack -> HomeGoalStatusLabel.OnTrack
    GoalStatus.OffTrack -> HomeGoalStatusLabel.AtRisk
    GoalStatus.Completed -> HomeGoalStatusLabel.GoalMet
    GoalStatus.NotEnoughData -> HomeGoalStatusLabel.NeedsBaseline
}


private fun Double.toCompactPercent(): String {
    val rounded = (this * 10).toInt() / 10.0
    return if (rounded % 1.0 == 0.0) "${rounded.toInt()}%" else "$rounded%"
}

private fun Int.toDurationLabel(): String {
    val hours = this / 60
    val minutes = this % 60
    return when {
        hours <= 0 -> "${minutes}m"
        minutes == 0 -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
}

private fun Int.toTwoDigits(): String = toString().padStart(2, '0')

fun activeDayWindow(
    dayStartHour: Int,
    bedtimeHour: Int,
    awakeMinutesPerDay: Int,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): ActiveDayWindow {
    if (dayStartHour !in 0..23 || bedtimeHour !in 0..23 || awakeMinutesPerDay <= 0) {
        return ActiveDayWindow(elapsedMinutes = 0, remainingMinutes = awakeMinutesPerDay.coerceAtLeast(0))
    }
    val localTime = now.toLocalDateTime(timeZone).time
    val currentMinutes = localTime.hour * 60 + localTime.minute
    val dayStartMinutes = dayStartHour * 60
    val bedtimeMinutes = bedtimeHour * 60

    if (bedtimeHour > dayStartHour) {
        return when {
            currentMinutes <= dayStartMinutes -> ActiveDayWindow(0, awakeMinutesPerDay)
            currentMinutes >= bedtimeMinutes -> ActiveDayWindow(awakeMinutesPerDay, 0)
            else -> {
                val elapsed = currentMinutes - dayStartMinutes
                ActiveDayWindow(elapsed, (awakeMinutesPerDay - elapsed).coerceAtLeast(0))
            }
        }
    }

    val minutesSinceStart = (currentMinutes - dayStartMinutes).mod(24 * 60)
    return if (minutesSinceStart >= awakeMinutesPerDay) {
        ActiveDayWindow(awakeMinutesPerDay, 0)
    } else {
        ActiveDayWindow(minutesSinceStart, (awakeMinutesPerDay - minutesSinceStart).coerceAtLeast(0))
    }
}

private fun expectedSmokesByNow(
    elapsedActiveMinutes: Int,
    awakeMinutesPerDay: Int,
    dailyCap: Int,
): Float {
    if (awakeMinutesPerDay <= 0 || dailyCap <= 0) return 0f
    return (elapsedActiveMinutes.toFloat() / awakeMinutesPerDay.toFloat()) * dailyCap.toFloat()
}

private fun dailyCapRemainingGapMinutes(
    remainingCigarettes: Int,
    remainingActiveMinutes: Int,
): Int? {
    if (remainingCigarettes <= 0 || remainingActiveMinutes <= 0) return null
    return (remainingActiveMinutes / remainingCigarettes).coerceAtLeast(1)
}

private fun Long?.toDebugMinutesLabel(): String = this?.let { "${it}m" } ?: "null"

private fun Int?.toDebugMinutesLabel(): String = this?.let { "${it}m" } ?: "null"

private fun Float.toPercentLabel(): String = "${(this * 100).toInt()}%"

private fun Long?.toSignedDebugMinutesLabel(): String = when {
    this == null -> "null"
    this >= 0 -> "+${this}m"
    else -> "${this}m"
}
