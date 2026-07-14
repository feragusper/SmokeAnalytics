package com.feragusper.smokeanalytics.features.goals.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.goals.domain.GoalStatus
import com.feragusper.smokeanalytics.features.goals.presentation.web.mvi.GoalsIntent
import com.feragusper.smokeanalytics.features.goals.presentation.web.mvi.GoalsWebStore
import com.feragusper.smokeanalytics.libraries.design.EmptyStateCard
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonCard
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.i18n.AppStrings
import com.feragusper.smokeanalytics.libraries.design.i18n.LocalStrings
import com.feragusper.smokeanalytics.libraries.design.StatusTone
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun GoalsWebScreen(
    deps: GoalsWebDependencies,
    onNavigateBack: () -> Unit,
) {
    val store = remember(deps) { GoalsWebStore(processHolder = deps.processHolder) }

    LaunchedEffect(store) { store.start() }

    val state by store.state.collectAsState()

    // Whether the goal editor (selector + setup) is shown instead of the progress view.
    var configuring by remember { mutableStateOf(false) }
    val strings = LocalStrings.current

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = strings.goalsTitle,
            eyebrow = strings.goalsTitle,
            subtitle = if (configuring) {
                strings.chooseOneTarget
            } else {
                strings.trackHowGoing
            },
            badgeText = when {
                state.displayLoading -> strings.loading
                state.errorMessage != null -> strings.needsAttention
                state.preferences.activeGoal != null -> strings.activeGoal
                else -> strings.noGoalYet
            },
            badgeTone = when {
                state.displayLoading -> StatusTone.Busy
                state.errorMessage != null -> StatusTone.Error
                else -> StatusTone.Default
            },
        )

        // Skeleton during the initial load so the sign-in / empty state never flashes.
        if (state.displayLoading && state.currentEmail == null && state.errorMessage == null) {
            LoadingSkeletonCard(heightPx = 176, lineWidths = listOf("30%", "68%", "44%"))
            LoadingSkeletonCard(heightPx = 180, lineWidths = listOf("22%", "58%", "46%"))
            return@Div
        }

        when {
            // Logged out: the editor panel hosts the sign-in card.
            state.currentEmail == null -> {
                GoalsWebEditorPanel(
                    currentEmail = state.currentEmail,
                    preferences = state.preferences,
                    goalProgress = state.goalProgress,
                    displayLoading = state.displayLoading,
                    onSaveGoal = { goal -> store.send(GoalsIntent.SaveGoal(goal)) },
                    onClearGoal = { store.send(GoalsIntent.ClearGoal) },
                    onSignInSuccess = { store.send(GoalsIntent.FetchGoals) },
                    onSignInError = { store.send(GoalsIntent.FetchGoals) },
                )
            }

            configuring -> {
                GhostButton(text = strings.backToProgress, onClick = { configuring = false })
                GoalsWebEditorPanel(
                    currentEmail = state.currentEmail,
                    preferences = state.preferences,
                    goalProgress = state.goalProgress,
                    displayLoading = state.displayLoading,
                    onSaveGoal = { goal -> store.send(GoalsIntent.SaveGoal(goal)) },
                    onClearGoal = { store.send(GoalsIntent.ClearGoal) },
                    onSignInSuccess = { store.send(GoalsIntent.FetchGoals) },
                    onSignInError = { store.send(GoalsIntent.FetchGoals) },
                )
            }

            state.preferences.activeGoal == null -> {
                SurfaceCard {
                    Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;") }) {
                        Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(strings.noActiveGoalYet) }
                        Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                            Text(strings.noActiveGoalBody)
                        }
                        PrimaryButton(text = strings.setAGoal, onClick = { configuring = true })
                    }
                }
            }

            else -> GoalProgressPanel(
                goalProgress = state.goalProgress,
                onConfigure = { configuring = true },
            )
        }

        state.errorMessage?.let { msg ->
            EmptyStateCard(
                title = strings.goalsUnavailable,
                message = msg,
                actionLabel = strings.tryAgain,
                onAction = { store.send(GoalsIntent.FetchGoals) },
            )
        }

        state.infoMessage?.let { msg ->
            Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(msg) }
        }
    }
}

@Composable
private fun GoalProgressPanel(
    goalProgress: GoalProgress?,
    onConfigure: () -> Unit,
) {
    val strings = LocalStrings.current
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:10px;") }) {
            Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(strings.activeGoal) }
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                Text(goalProgress?.titleKind?.text(strings) ?: strings.yourGoal)
            }
            goalProgress?.let { progress ->
                progress.status.label(strings)?.let { status ->
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(status) }
                }
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) { Text(progress.progress.text(strings)) }
                progress.supporting.text(strings)?.let {
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(it) }
                }
                progress.baseline?.let {
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(it.text(strings)) }
                }
                if (progress.hasStreak) {
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(strings.consistencyStreakDays(progress.streakDays)) }
                }
            }
            PrimaryButton(text = strings.configureGoal, onClick = onConfigure)
        }
    }
}

private fun GoalStatus.label(strings: AppStrings): String? = when (this) {
    GoalStatus.OnTrack -> strings.onTrack
    GoalStatus.OffTrack -> strings.offTrack
    GoalStatus.Completed -> strings.completed
    GoalStatus.NotEnoughData -> null
}
