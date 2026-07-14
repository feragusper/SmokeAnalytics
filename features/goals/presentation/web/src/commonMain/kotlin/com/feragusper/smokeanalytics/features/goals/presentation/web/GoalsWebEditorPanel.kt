package com.feragusper.smokeanalytics.features.goals.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.GoogleSignInComponentWeb
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.i18n.AppStrings
import com.feragusper.smokeanalytics.libraries.design.i18n.LocalStrings
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import com.feragusper.smokeanalytics.libraries.preferences.domain.GoalType
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text

@Composable
fun GoalsWebEditorPanel(
    currentEmail: String?,
    preferences: UserPreferences,
    goalProgress: GoalProgress?,
    displayLoading: Boolean,
    onSaveGoal: (SmokingGoal) -> Unit,
    onClearGoal: () -> Unit,
    onSignInSuccess: () -> Unit,
    onSignInError: (Throwable) -> Unit,
) {
    var selectedType by remember(preferences.activeGoal) {
        mutableStateOf(preferences.activeGoal?.type ?: GoalType.DailyCap)
    }
    var draftValue by remember(preferences.activeGoal) { mutableStateOf(preferences.activeGoal.defaultDraftValue()) }

    LaunchedEffect(preferences.activeGoal) {
        selectedType = preferences.activeGoal?.type ?: GoalType.DailyCap
        draftValue = preferences.activeGoal.defaultDraftValue()
    }

    val draftGoal = selectedType.toGoalOrNull(draftValue)
    val strings = LocalStrings.current

    if (currentEmail == null) {
        SurfaceCard {
            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;") }) {
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(strings.goalsNeedAccount) }
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                    Text(strings.goalsNeedAccountBody)
                }
                GoogleSignInComponentWeb(
                    onSignInSuccess = onSignInSuccess,
                    onSignInError = onSignInError,
                )
            }
        }
        return
    }

    SurfaceCard {
        Div(attrs = { attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:12px;") }) {
            GoalType.entries.forEach { type ->
                if (selectedType == type) {
                    PrimaryButton(
                        text = "✓ " + type.label(strings),
                        onClick = {},
                    )
                } else {
                    GhostButton(
                        text = type.label(strings),
                        onClick = {
                            selectedType = type
                            draftValue = type.defaultDraftValue()
                        },
                    )
                }
            }
        }
    }

    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;") }) {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(strings.goalSetup) }
            Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(selectedType.inputHelp(strings)) }
            Input(type = if (selectedType == GoalType.DailyCap || selectedType == GoalType.MindfulGap) InputType.Number else InputType.Text, attrs = {
                classes(SmokeWebStyles.dateInput)
                value(draftValue)
                if (displayLoading) disabled()
                onInput { draftValue = it.value?.toString() ?: "" }
            })
            draftGoal?.let {
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) { Text(it.summaryLabel(strings)) }
            }
            goalProgress?.let {
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) { Text(it.progress.text(strings)) }
                it.baseline?.let { baseline ->
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(baseline.text(strings)) }
                }
            }
            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                GhostButton(
                    text = strings.clear,
                    onClick = onClearGoal,
                    enabled = !displayLoading && preferences.activeGoal != null,
                )
                PrimaryButton(
                    text = if (preferences.activeGoal == null) strings.saveGoal else strings.updateGoal,
                    onClick = { draftGoal?.let(onSaveGoal) },
                    enabled = !displayLoading && draftGoal != null,
                )
            }
        }
    }
}

private fun GoalType.label(strings: AppStrings): String = when (this) {
    GoalType.DailyCap -> strings.dailyCap
    GoalType.ReductionVsPreviousWeek -> strings.reduceVsPrevWeek
    GoalType.ReductionVsPreviousMonth -> strings.reduceVsPrevMonth
    GoalType.MindfulGap -> strings.mindfulGap
}

private fun GoalType.defaultDraftValue(): String = when (this) {
    GoalType.DailyCap -> "8"
    GoalType.ReductionVsPreviousWeek, GoalType.ReductionVsPreviousMonth -> "15"
    GoalType.MindfulGap -> "90"
}

private fun GoalType.inputHelp(strings: AppStrings): String = when (this) {
    GoalType.DailyCap -> strings.usePositiveWhole
    GoalType.ReductionVsPreviousWeek, GoalType.ReductionVsPreviousMonth -> strings.useValue1to90
    GoalType.MindfulGap -> strings.usePositiveMinutes
}

private fun SmokingGoal?.defaultDraftValue(): String = when (this) {
    is SmokingGoal.DailyCap -> maxCigarettesPerDay.toString()
    is SmokingGoal.ReductionVsPreviousWeek -> reductionPercent.toString()
    is SmokingGoal.ReductionVsPreviousMonth -> reductionPercent.toString()
    is SmokingGoal.MindfulGap -> targetMinutes.toString()
    null -> GoalType.DailyCap.defaultDraftValue()
}

private fun GoalType.toGoalOrNull(value: String): SmokingGoal? = when (this) {
    GoalType.DailyCap -> value.toIntOrNull()?.takeIf { it > 0 }?.let(SmokingGoal::DailyCap)
    GoalType.ReductionVsPreviousWeek -> value.toDoubleOrNull()?.takeIf { it in 1.0..90.0 }?.let(SmokingGoal::ReductionVsPreviousWeek)
    GoalType.ReductionVsPreviousMonth -> value.toDoubleOrNull()?.takeIf { it in 1.0..90.0 }?.let(SmokingGoal::ReductionVsPreviousMonth)
    GoalType.MindfulGap -> value.toIntOrNull()?.takeIf { it > 0 }?.let(SmokingGoal::MindfulGap)
}

private fun SmokingGoal.summaryLabel(strings: AppStrings): String = when (this) {
    is SmokingGoal.DailyCap -> strings.dailyCapDesc(maxCigarettesPerDay)
    is SmokingGoal.ReductionVsPreviousWeek -> strings.reduceWeekDesc(reductionPercent.toInt())
    is SmokingGoal.ReductionVsPreviousMonth -> strings.reduceMonthDesc(reductionPercent.toInt())
    is SmokingGoal.MindfulGap -> strings.waitAtLeastDesc(targetMinutes)
}
