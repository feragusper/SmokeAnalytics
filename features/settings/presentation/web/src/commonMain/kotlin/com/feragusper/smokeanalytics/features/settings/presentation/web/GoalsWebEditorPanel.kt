package com.feragusper.smokeanalytics.features.settings.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
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
    onBack: () -> Unit,
    onSaveGoal: (SmokingGoal) -> Unit,
    onClearGoal: () -> Unit,
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

    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:16px;") }) {
            GhostButton(text = "Back to You", onClick = onBack)
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Goals") }
            Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                Text("Choose one active target and keep its progress visible from Home.")
            }
        }
    }

    if (currentEmail == null) {
        SurfaceCard {
            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;") }) {
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Goals need an account") }
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                    Text("Sign in from You to save one active goal and sync it across platforms.")
                }
            }
        }
        return
    }

    SurfaceCard {
        Div(attrs = { attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:12px;") }) {
            GoalType.entries.forEach { type ->
                GhostButton(
                    text = type.label(),
                    onClick = {
                        selectedType = type
                        draftValue = type.defaultDraftValue()
                    },
                )
            }
        }
    }

    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;") }) {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Goal setup") }
            Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(selectedType.inputHelp()) }
            Input(type = if (selectedType == GoalType.DailyCap || selectedType == GoalType.MindfulGap) InputType.Number else InputType.Text, attrs = {
                classes(SmokeWebStyles.dateInput)
                value(draftValue)
                if (displayLoading) disabled()
                onInput { draftValue = it.value?.toString() ?: "" }
            })
            draftGoal?.let {
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) { Text(it.summaryLabel()) }
            }
            goalProgress?.let {
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) { Text(it.progressLabel) }
                it.baselineLabel?.let { baseline ->
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(baseline) }
                }
            }
            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                GhostButton(
                    text = "Clear",
                    onClick = onClearGoal,
                    enabled = !displayLoading && preferences.activeGoal != null,
                )
                PrimaryButton(
                    text = if (preferences.activeGoal == null) "Save goal" else "Update goal",
                    onClick = { draftGoal?.let(onSaveGoal) },
                    enabled = !displayLoading && draftGoal != null,
                )
            }
        }
    }
}

private fun GoalType.label(): String = when (this) {
    GoalType.DailyCap -> "Daily cap"
    GoalType.ReductionVsPreviousWeek -> "Reduce vs previous week"
    GoalType.ReductionVsPreviousMonth -> "Reduce vs previous month"
    GoalType.MindfulGap -> "Mindful gap"
}

private fun GoalType.defaultDraftValue(): String = when (this) {
    GoalType.DailyCap -> "8"
    GoalType.ReductionVsPreviousWeek, GoalType.ReductionVsPreviousMonth -> "15"
    GoalType.MindfulGap -> "90"
}

private fun GoalType.inputHelp(): String = when (this) {
    GoalType.DailyCap -> "Use a positive whole number."
    GoalType.ReductionVsPreviousWeek, GoalType.ReductionVsPreviousMonth -> "Use a value between 1 and 90."
    GoalType.MindfulGap -> "Use a positive number of minutes."
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

private fun SmokingGoal.summaryLabel(): String = when (this) {
    is SmokingGoal.DailyCap -> "Daily cap: at most $maxCigarettesPerDay cigarettes."
    is SmokingGoal.ReductionVsPreviousWeek -> "Reduce the current week by $reductionPercent%."
    is SmokingGoal.ReductionVsPreviousMonth -> "Reduce the current month by $reductionPercent%."
    is SmokingGoal.MindfulGap -> "Wait at least $targetMinutes minutes between cigarettes."
}
