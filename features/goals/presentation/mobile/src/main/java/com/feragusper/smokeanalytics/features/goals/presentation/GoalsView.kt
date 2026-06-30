package com.feragusper.smokeanalytics.features.goals.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/** Goals tab landing screen: shows how the active goal is going. */
@Composable
fun GoalsView(
    viewModel: GoalsViewModel,
    navigateToConfigure: () -> Unit,
) {
    val viewState by remember(viewModel) { viewModel.states() }.collectAsState()

    viewState.Compose(
        intent = { intent -> viewModel.intents().trySend(intent) },
        navigateToConfigure = navigateToConfigure,
    )
}

/** Goal editor (selector + setup), reached from [GoalsView] via "Configure goal". */
@Composable
fun GoalsConfigureView(
    viewModel: GoalsViewModel,
    navigateBack: () -> Unit,
) {
    val viewState by remember(viewModel) { viewModel.states() }.collectAsState()

    viewState.ComposeEditor(
        intent = { intent -> viewModel.intents().trySend(intent) },
        navigateBack = navigateBack,
    )
}
