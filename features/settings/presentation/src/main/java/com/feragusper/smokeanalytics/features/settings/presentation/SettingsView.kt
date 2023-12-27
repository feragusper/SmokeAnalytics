package com.feragusper.smokeanalytics.features.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@Composable
fun SettingsView(viewModel: SettingsViewModel) {
    val viewState by remember(viewModel) { viewModel.states() }.collectAsState()
    viewState.Compose { intent -> viewModel.intents().trySend(intent) }
}