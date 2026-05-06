package com.feragusper.smokeanalytics.features.goals.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@Composable
fun GoalsView(
    viewModel: GoalsViewModel,
    navigateBack: () -> Unit,
) {
    val viewState by remember(viewModel) { viewModel.states() }.collectAsState()

    viewState.Compose(
        intent = { intent -> viewModel.intents().trySend(intent) },
        navigateBack = navigateBack,
    )
}
