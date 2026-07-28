package com.feragusper.smokeanalytics.features.goals.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsScreen
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTarget
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTracker
import org.koin.compose.koinInject
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.GoogleSignInComponent
import com.feragusper.smokeanalytics.libraries.preferences.domain.GoalType
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences

/**
 * Full-screen goal editor (selector + setup). Shown above the bottom bar with a back
 * navigation and a loading skeleton so the sign-in state never flashes. The goal type list
 * includes a "no goal" choice, and the save/remove action lives in a screen-level bottom bar
 * rather than inside the setup card.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    val analytics = koinInject<AnalyticsTracker>()
    // null = the "no goal" choice.
    var selectedType by remember(preferences.activeGoal) {
        mutableStateOf<GoalType?>(preferences.activeGoal?.type ?: GoalType.DailyCap)
    }
    var draftValue by remember(preferences.activeGoal) { mutableStateOf(preferences.activeGoal.defaultDraftValue()) }

    LaunchedEffect(preferences.activeGoal) {
        selectedType = preferences.activeGoal?.type ?: GoalType.DailyCap
        draftValue = preferences.activeGoal.defaultDraftValue()
    }

    val draftGoal = selectedType?.toGoalOrNull(draftValue)
    val backgroundColor = MaterialTheme.colorScheme.background

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.goals_configure_goal)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.goals_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    scrolledContainerColor = backgroundColor,
                ),
            )
        },
        bottomBar = {
            // Hidden only while the initial skeleton shows (currentEmail not yet resolved).
            if (currentEmail != null) {
                GoalsEditorBottomAction(
                    isNoGoal = selectedType == null,
                    isCreating = preferences.activeGoal == null,
                    hasActiveGoal = preferences.activeGoal != null,
                    canSave = draftGoal != null,
                    loading = displayLoading,
                    onSave = { draftGoal?.let(onSaveGoal) },
                    onRemove = onClearGoal,
                    backgroundColor = backgroundColor,
                )
            }
        },
    ) { padding ->
        // Skeleton while the session/goal loads, so the sign-in card never flashes.
        if (displayLoading && currentEmail == null && errorMessage == null) {
            GoalsEditorSkeleton(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = stringResource(R.string.goals_choose_target),
                    modifier = Modifier.padding(horizontal = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                errorMessage?.let { message ->
                    Card(
                        shape = RoundedCornerShape(24.dp),
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
                            Text(text = message, style = MaterialTheme.typography.bodyMedium)
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
                                    shape = RoundedCornerShape(24.dp),
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
                                        Text(text = message, style = MaterialTheme.typography.bodyMedium)
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
                } else {
                    GoalsPanelCard {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = stringResource(R.string.goals_goal_type),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            GoalChoiceCard(
                                title = stringResource(R.string.goals_no_goal_option),
                                description = stringResource(R.string.goals_no_goal_option_body),
                                selected = selectedType == null,
                                onClick = { selectedType = null },
                            )
                            GoalType.entries.forEach { type ->
                                GoalChoiceCard(
                                    title = type.label(),
                                    description = type.description(),
                                    selected = selectedType == type,
                                    onClick = {
                                        analytics.buttonTap(AnalyticsScreen.GOALS_CONFIGURE, AnalyticsTarget.SELECT_GOAL_TYPE)
                                        selectedType = type
                                        draftValue = type.defaultDraftValue()
                                    },
                                )
                            }
                        }
                    }

                    selectedType?.let { type ->
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
                                    label = { Text(type.inputLabel()) },
                                    supportingText = { Text(type.inputHelp()) },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = if (type == GoalType.DailyCap || type == GoalType.MindfulGap) {
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
                                        text = progress.progress.text(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    progress.baseline?.let { baseline ->
                                        Text(
                                            text = baseline.text(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    progress.warning?.let { warning ->
                                        Text(
                                            text = warning.text(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        )
                                    }
                                    progress.celebration?.let { celebration ->
                                        Text(
                                            text = celebration.text(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    if (progress.hasStreak) {
                                        Text(
                                            text = goalStreakText(progress.streakDays),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalsEditorBottomAction(
    isNoGoal: Boolean,
    isCreating: Boolean,
    hasActiveGoal: Boolean,
    canSave: Boolean,
    loading: Boolean,
    onSave: () -> Unit,
    onRemove: () -> Unit,
    backgroundColor: androidx.compose.ui.graphics.Color,
) {
    Surface(color = backgroundColor) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            if (isNoGoal) {
                Button(
                    onClick = onRemove,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    enabled = !loading && hasActiveGoal,
                ) {
                    Text(stringResource(R.string.goals_remove_goal), fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    enabled = !loading && canSave,
                ) {
                    Text(
                        text = if (isCreating) stringResource(R.string.goals_save_goal) else stringResource(R.string.goals_update_goal),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalsEditorSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        repeat(2) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    EditorSkeletonLine(widthFraction = 0.4f)
                    EditorSkeletonLine(widthFraction = 0.8f, height = 26.dp)
                    EditorSkeletonLine(widthFraction = 1f, height = 44.dp)
                }
            }
        }
    }
}

@Composable
private fun EditorSkeletonLine(widthFraction: Float, height: Dp = 14.dp) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)),
    )
}

@Composable
private fun GoalChoiceCard(
    title: String,
    description: String,
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
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
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
        shape = RoundedCornerShape(28.dp),
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

@Composable
private fun SmokingGoal.summaryLabel(): String = when (this) {
    is SmokingGoal.DailyCap ->
        stringResource(R.string.goals_summary_daily_cap, maxCigarettesPerDay)
    is SmokingGoal.ReductionVsPreviousWeek ->
        stringResource(R.string.goals_summary_reduce_week, reductionPercent.asPercentLabel())
    is SmokingGoal.ReductionVsPreviousMonth ->
        stringResource(R.string.goals_summary_reduce_month, reductionPercent.asPercentLabel())
    is SmokingGoal.MindfulGap ->
        stringResource(R.string.goals_summary_mindful_gap, targetMinutes)
}

/** Percent as a plain label: whole numbers without a decimal, otherwise one decimal. */
private fun Double.asPercentLabel(): String =
    if (this == toLong().toDouble()) toLong().toString() else "%.1f".format(this)
