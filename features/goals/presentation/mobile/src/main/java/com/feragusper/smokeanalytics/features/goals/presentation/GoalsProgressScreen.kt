package com.feragusper.smokeanalytics.features.goals.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsScreen
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTarget
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTracker
import org.koin.compose.koinInject
import androidx.compose.ui.res.stringResource
import com.feragusper.smokeanalytics.features.goals.presentation.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.goals.domain.GoalScore
import com.feragusper.smokeanalytics.features.goals.domain.GoalStatus
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.GoogleSignInComponent
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal

/**
 * The Goals tab landing screen: focuses on how the active goal is going, not on the
 * selector. While loading it shows a skeleton (so the sign-in/empty state never flashes),
 * and a stringResource(R.string.goals_configure_goal) button leads to [GoalsEditorScreen].
 */
@Composable
fun GoalsProgressScreen(
    currentEmail: String?,
    activeGoal: SmokingGoal?,
    goalProgress: GoalProgress?,
    displayLoading: Boolean,
    errorMessage: String? = null,
    signInErrorMessage: String? = null,
    onConfigure: () -> Unit,
    onSignInSuccess: () -> Unit,
    onSignInError: (String) -> Unit,
) {
    val analytics = koinInject<AnalyticsTracker>()
    val onConfigureTracked = {
        analytics.buttonTap(AnalyticsScreen.GOALS, AnalyticsTarget.CONFIGURE_GOAL)
        onConfigure()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.goals_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.goals_track_how),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (currentEmail != null) {
                FilledTonalIconButton(onClick = onConfigureTracked) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.goals_configure_goal),
                    )
                }
            }
        }

        // Skeleton during the initial load so the sign-in / empty state never flashes.
        if (displayLoading && goalProgress == null && currentEmail == null && errorMessage == null) {
            GoalsSkeleton()
            return
        }

        errorMessage?.let { message ->
            GoalsCard(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Text(stringResource(R.string.goals_could_not_load), style = MaterialTheme.typography.titleMedium)
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (currentEmail == null) {
            GoalsCard {
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
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                GoogleSignInComponent(
                    modifier = Modifier.fillMaxWidth(),
                    onSignInSuccess = onSignInSuccess,
                    onSignInError = onSignInError,
                )
            }
            return
        }

        if (activeGoal == null) {
            GoalsCard {
                Text(
                    text = stringResource(R.string.goals_no_active_goal),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.goals_no_active_goal_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onConfigureTracked,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(stringResource(R.string.goals_set_a_goal), fontWeight = FontWeight.Bold)
                }
            }
            return
        }

        GoalProgressContent(goalProgress = goalProgress)
    }
}

/**
 * Renders the active goal as a small dashboard of cards — a focal "today" card plus one
 * card per meaningful datum (streak, weekly/monthly score) and any celebration/warning note.
 */
@Composable
private fun GoalProgressContent(goalProgress: GoalProgress?) {
    // Focal card: today's progress toward the active goal.
    GoalsCard {
        Text(
            text = stringResource(R.string.goals_active_goal),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = goalProgress?.titleKind?.text() ?: stringResource(R.string.goals_your_goal),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        goalProgress?.let { progress ->
            progress.status.pill()?.let { (label, container, content) ->
                Surface(color = container, shape = RoundedCornerShape(999.dp)) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = content,
                    )
                }
            }
            Text(
                text = progress.target.text(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            progress.progressFraction?.let { fraction ->
                LinearProgressIndicator(
                    progress = { fraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp)),
                )
            }
            Text(
                text = progress.progress.text(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            progress.supporting.textOrNull()?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    val progress = goalProgress ?: return

    // Celebration / warning, each as its own note card.
    progress.celebration?.let {
        GoalNoteCard(
            text = it.text(),
            icon = Icons.Filled.Star,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
    progress.warning?.let {
        GoalNoteCard(
            text = it.text(),
            icon = Icons.Filled.Warning,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
    }

    // Streak as its own stat card.
    if (progress.hasStreak) {
        StreakStatCard(days = progress.streakDays)
    }

    // Weekly / monthly score cards, side by side and equal height.
    val week = progress.weeklyScore?.takeIf { it.trackedDays > 0 }
    val month = progress.monthlyScore?.takeIf { it.trackedDays > 0 }
    if (week != null || month != null) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            week?.let {
                ScoreStatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.goals_stat_week),
                    score = it,
                )
            }
            month?.let {
                ScoreStatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.goals_stat_month),
                    score = it,
                )
            }
        }
    }

    // Baseline context, as a quiet footnote.
    progress.baseline?.let {
        Text(
            text = it.text(),
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StreakStatCard(days: Int) {
    GoalsCard {
        Text(
            text = stringResource(R.string.goals_stat_streak),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = days.toString(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.goals_stat_streak_caption),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ScoreStatCard(
    modifier: Modifier = Modifier,
    label: String,
    score: GoalScore,
) {
    GoalsCard(modifier = modifier.fillMaxHeight()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.goals_stat_points, score.points),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.goals_stat_days_on_goal, score.completedDays),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GoalNoteCard(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
) {
    GoalsCard(containerColor = containerColor, contentColor = contentColor) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun GoalsSkeleton() {
    repeat(2) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonLine(widthFraction = 0.4f)
                SkeletonLine(widthFraction = 0.7f, height = 26.dp)
                SkeletonLine(widthFraction = 1f, height = 8.dp)
                SkeletonLine(widthFraction = 0.5f)
            }
        }
    }
}

@Composable
private fun SkeletonLine(widthFraction: Float, height: androidx.compose.ui.unit.Dp = 14.dp) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)),
    )
}

@Composable
private fun GoalsCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun GoalStatus.pill(): Triple<String, Color, Color>? = when (this) {
    GoalStatus.OnTrack -> Triple(
        stringResource(R.string.goals_on_track),
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.onPrimaryContainer,
    )
    GoalStatus.OffTrack -> Triple(
        stringResource(R.string.goals_off_track),
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.onErrorContainer,
    )
    GoalStatus.Completed -> Triple(
        stringResource(R.string.goals_completed),
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.onTertiaryContainer,
    )
    GoalStatus.NotEnoughData -> null
}
