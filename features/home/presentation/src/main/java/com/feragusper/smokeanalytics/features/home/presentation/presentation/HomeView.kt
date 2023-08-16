package com.feragusper.smokeanalytics.features.home.presentation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.feragusper.smokeanalytics.features.home.presentation.presentation.HomeViewModel

@Composable
fun HomeView(viewModel: HomeViewModel) {
    val viewState by remember(viewModel) { viewModel.states() }.collectAsState()
    viewState.Compose { intent -> viewModel.intents().trySend(intent) }
}