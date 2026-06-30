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

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "Goals",
            eyebrow = "Goals",
            subtitle = if (configuring) {
                "Choose one active target and keep its progress visible from Home."
            } else {
                "Track how your active goal is going."
            },
            badgeText = when {
                state.displayLoading -> "Loading"
                state.errorMessage != null -> "Needs attention"
                state.preferences.activeGoal != null -> "Active goal"
                else -> "No goal yet"
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
                GhostButton(text = "← Back to progress", onClick = { configuring = false })
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
                        Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("No active goal yet") }
                        Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                            Text("Set one target to keep your progress front and center on Home.")
                        }
                        PrimaryButton(text = "Set a goal", onClick = { configuring = true })
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
                title = "Goals are unavailable",
                message = msg,
                actionLabel = "Try again",
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
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:10px;") }) {
            Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text("Active goal") }
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                Text(goalProgress?.title ?: "Your goal")
            }
            goalProgress?.let { progress ->
                progress.status.label()?.let { status ->
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(status) }
                }
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) { Text(progress.progressLabel) }
                progress.supportingText.takeIf { it.isNotBlank() }?.let {
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(it) }
                }
                progress.baselineLabel?.let {
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(it) }
                }
                progress.streakLabel?.let {
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(it) }
                }
            }
            PrimaryButton(text = "Configure goal", onClick = onConfigure)
        }
    }
}

private fun GoalStatus.label(): String? = when (this) {
    GoalStatus.OnTrack -> "On track"
    GoalStatus.OffTrack -> "Off track"
    GoalStatus.Completed -> "Completed"
    GoalStatus.NotEnoughData -> null
}
