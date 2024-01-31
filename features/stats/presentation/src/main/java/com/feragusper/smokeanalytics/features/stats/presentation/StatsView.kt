package com.feragusper.smokeanalytics.features.stats.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@Composable
fun StatsView(viewModel: StatsViewModel) {
    val viewState by remember(viewModel) { viewModel.states() }.collectAsState()
    viewState.Compose { intent -> viewModel.intents().trySend(intent) }
}