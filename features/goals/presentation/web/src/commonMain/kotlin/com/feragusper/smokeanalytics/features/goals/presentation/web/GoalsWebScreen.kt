package com.feragusper.smokeanalytics.features.goals.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.feragusper.smokeanalytics.features.goals.presentation.web.mvi.GoalsIntent
import com.feragusper.smokeanalytics.features.goals.presentation.web.mvi.GoalsWebStore
import com.feragusper.smokeanalytics.libraries.design.EmptyStateCard
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonCard
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
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

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        if (state.displayLoading && state.currentEmail == null && state.errorMessage == null) {
            LoadingSkeletonCard(heightPx = 176, lineWidths = listOf("30%", "68%", "44%"))
            LoadingSkeletonCard(heightPx = 180, lineWidths = listOf("22%", "58%", "46%"))
            return@Div
        }

        GoalsWebEditorPanel(
            currentEmail = state.currentEmail,
            preferences = state.preferences,
            goalProgress = state.goalProgress,
            displayLoading = state.displayLoading,
            onBack = onNavigateBack,
            onSaveGoal = { goal -> store.send(GoalsIntent.SaveGoal(goal)) },
            onClearGoal = { store.send(GoalsIntent.ClearGoal) },
            onSignInSuccess = { store.send(GoalsIntent.FetchGoals) },
            onSignInError = { store.send(GoalsIntent.FetchGoals) },
        )

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
