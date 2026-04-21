package com.feragusper.smokeanalytics.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.GoogleSignInComponent
import com.feragusper.smokeanalytics.libraries.preferences.domain.GoalType
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences

@Composable
fun GoalsEditorScreen(
    currentEmail: String?,
    preferences: UserPreferences,
    goalProgress: GoalProgress?,
    displayLoading: Boolean,
    errorMessage: String? = null,
    onBack: () -> Unit,
    onSaveGoal: (SmokingGoal) -> Unit,
    onClearGoal: () -> Unit,
    onSignInSuccess: () -> Unit,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        GoalsPanelCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onBack) {
                    Text("Back to You")
                }
                Text(
                    text = "Goals",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Choose one active target and keep its progress visible from Home.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        errorMessage?.let { message ->
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Could not update your goal",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        if (currentEmail == null) {
            GoalsPanelCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Goals need an account",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Sign in to save one active goal and keep it aligned across mobile and web.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GoogleSignInComponent(
                        modifier = Modifier.fillMaxWidth(),
                        onSignInSuccess = onSignInSuccess,
                        onSignInError = {},
                    )
                }
            }
            return
        }

        GoalsPanelCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Goal type",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                GoalType.entries.forEach { type ->
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            selectedType = type
                            draftValue = type.defaultDraftValue()
                        },
                    ) {
                        Text(type.label())
                    }
                }
            }
        }

        GoalsPanelCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Goal setup",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                OutlinedTextField(
                    value = draftValue,
                    onValueChange = { draftValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(selectedType.inputLabel()) },
                    supportingText = { Text(selectedType.inputHelp()) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (selectedType == GoalType.DailyCap || selectedType == GoalType.MindfulGap) {
                            KeyboardType.Number
                        } else {
                            KeyboardType.Decimal
                        }
                    ),
                    singleLine = true,
                )

                draftGoal?.let { goal ->
                    Text(
                        text = goal.summaryLabel(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                goalProgress?.let { progress ->
                    progress.progressFraction?.let { fraction ->
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Text(
                        text = progress.progressLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    progress.baselineLabel?.let { baseline ->
                        Text(
                            text = baseline,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    progress.warningLabel?.let { warning ->
                        Text(
                            text = warning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                    progress.celebrationLabel?.let { celebration ->
                        Text(
                            text = celebration,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    progress.streakLabel?.let { streak ->
                        Text(
                            text = streak,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onClearGoal,
                        enabled = !displayLoading && preferences.activeGoal != null,
                    ) {
                        Text("Clear")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { draftGoal?.let(onSaveGoal) },
                        enabled = !displayLoading && draftGoal != null,
                    ) {
                        Text(if (preferences.activeGoal == null) "Save goal" else "Update goal")
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalsPanelCard(
    content: @Composable () -> Unit,
) {
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
    }
}

private fun GoalType.label(): String = when (this) {
    GoalType.DailyCap -> "Daily cap"
    GoalType.ReductionVsPreviousWeek -> "Reduce vs previous week"
    GoalType.ReductionVsPreviousMonth -> "Reduce vs previous month"
    GoalType.MindfulGap -> "Mindful gap"
}

private fun GoalType.inputLabel(): String = when (this) {
    GoalType.DailyCap -> "Max cigarettes per day"
    GoalType.ReductionVsPreviousWeek -> "Reduction percent"
    GoalType.ReductionVsPreviousMonth -> "Reduction percent"
    GoalType.MindfulGap -> "Target gap in minutes"
}

private fun GoalType.inputHelp(): String = when (this) {
    GoalType.DailyCap -> "Use a positive whole number."
    GoalType.ReductionVsPreviousWeek, GoalType.ReductionVsPreviousMonth -> "Use a value between 1 and 90."
    GoalType.MindfulGap -> "Use a positive number of minutes."
}

private fun GoalType.defaultDraftValue(): String = when (this) {
    GoalType.DailyCap -> "8"
    GoalType.ReductionVsPreviousWeek, GoalType.ReductionVsPreviousMonth -> "15"
    GoalType.MindfulGap -> "90"
}

private fun SmokingGoal?.defaultDraftValue(): String = when (this) {
    is SmokingGoal.DailyCap -> maxCigarettesPerDay.toString()
    is SmokingGoal.ReductionVsPreviousWeek -> reductionPercent.toString()
    is SmokingGoal.ReductionVsPreviousMonth -> reductionPercent.toString()
    is SmokingGoal.MindfulGap -> targetMinutes.toString()
    null -> GoalType.DailyCap.defaultDraftValue()
}

private fun GoalType.toGoalOrNull(value: String): SmokingGoal? = when (this) {
    GoalType.DailyCap -> value.toIntOrNull()
        ?.takeIf { it > 0 }
        ?.let(SmokingGoal::DailyCap)

    GoalType.ReductionVsPreviousWeek -> value.toDoubleOrNull()
        ?.takeIf { it in 1.0..90.0 }
        ?.let(SmokingGoal::ReductionVsPreviousWeek)

    GoalType.ReductionVsPreviousMonth -> value.toDoubleOrNull()
        ?.takeIf { it in 1.0..90.0 }
        ?.let(SmokingGoal::ReductionVsPreviousMonth)

    GoalType.MindfulGap -> value.toIntOrNull()
        ?.takeIf { it > 0 }
        ?.let(SmokingGoal::MindfulGap)
}

private fun SmokingGoal.summaryLabel(): String = when (this) {
    is SmokingGoal.DailyCap -> "Daily cap: at most $maxCigarettesPerDay cigarettes."
    is SmokingGoal.ReductionVsPreviousWeek -> "Reduce the current week by $reductionPercent%."
    is SmokingGoal.ReductionVsPreviousMonth -> "Reduce the current month by $reductionPercent%."
    is SmokingGoal.MindfulGap -> "Wait at least $targetMinutes minutes between cigarettes."
}
