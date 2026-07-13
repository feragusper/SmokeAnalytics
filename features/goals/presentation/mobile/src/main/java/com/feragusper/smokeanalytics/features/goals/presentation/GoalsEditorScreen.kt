package com.feragusper.smokeanalytics.features.goals.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.feragusper.smokeanalytics.features.goals.presentation.R
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
    signInErrorMessage: String? = null,
    onBack: () -> Unit,
    onSaveGoal: (SmokingGoal) -> Unit,
    onClearGoal: () -> Unit,
    onSignInSuccess: () -> Unit,
    onSignInError: (String) -> Unit,
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
        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.goals_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.goals_choose_target),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
                        text = stringResource(R.string.goals_could_not_update),
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
                        text = stringResource(R.string.goals_need_account),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.goals_sign_in_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    signInErrorMessage?.let { message ->
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
                                    text = stringResource(R.string.goals_sign_in_failed),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                    GoogleSignInComponent(
                        modifier = Modifier.fillMaxWidth(),
                        onSignInSuccess = onSignInSuccess,
                        onSignInError = onSignInError,
                    )
                }
            }
            return
        }

        GoalsPanelCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.goals_goal_type),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                GoalType.entries.forEach { type ->
                    GoalTypeCard(
                        type = type,
                        selected = selectedType == type,
                        onClick = {
                            selectedType = type
                            draftValue = type.defaultDraftValue()
                        },
                    )
                }
            }
        }

        GoalsPanelCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.goals_goal_setup),
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
                        Text(stringResource(R.string.goals_clear))
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { draftGoal?.let(onSaveGoal) },
                        enabled = !displayLoading && draftGoal != null,
                    ) {
                        Text(if (preferences.activeGoal == null) stringResource(R.string.goals_save_goal) else stringResource(R.string.goals_update_goal))
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalTypeCard(
    type: GoalType,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (selected) 2.dp else 1.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = type.label(),
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = type.description(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
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

@Composable
private fun GoalType.label(): String = when (this) {
    GoalType.DailyCap -> stringResource(R.string.goals_daily_cap)
    GoalType.ReductionVsPreviousWeek -> stringResource(R.string.goals_reduce_week)
    GoalType.ReductionVsPreviousMonth -> stringResource(R.string.goals_reduce_month)
    GoalType.MindfulGap -> stringResource(R.string.goals_mindful_gap)
}

@Composable
private fun GoalType.description(): String = when (this) {
    GoalType.DailyCap -> stringResource(R.string.goals_daily_cap_body)
    GoalType.ReductionVsPreviousWeek -> stringResource(R.string.goals_reduce_week_body)
    GoalType.ReductionVsPreviousMonth -> stringResource(R.string.goals_reduce_month_body)
    GoalType.MindfulGap -> stringResource(R.string.goals_mindful_gap_body)
}

@Composable
private fun GoalType.inputLabel(): String = when (this) {
    GoalType.DailyCap -> stringResource(R.string.goals_max_cigs_per_day)
    GoalType.ReductionVsPreviousWeek -> stringResource(R.string.goals_reduction_percent)
    GoalType.ReductionVsPreviousMonth -> stringResource(R.string.goals_reduction_percent)
    GoalType.MindfulGap -> stringResource(R.string.goals_target_gap_minutes)
}

@Composable
private fun GoalType.inputHelp(): String = when (this) {
    GoalType.DailyCap -> stringResource(R.string.goals_use_positive_whole)
    GoalType.ReductionVsPreviousWeek, GoalType.ReductionVsPreviousMonth -> stringResource(R.string.goals_use_1_90)
    GoalType.MindfulGap -> stringResource(R.string.goals_use_positive_minutes)
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
