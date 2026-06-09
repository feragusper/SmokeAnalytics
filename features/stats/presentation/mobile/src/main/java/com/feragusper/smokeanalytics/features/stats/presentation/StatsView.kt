package com.feragusper.smokeanalytics.features.stats.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.compose.StatsViewState
import java.time.LocalDate as JavaLocalDate

@Composable
fun StatsView(
    viewModel: StatsViewModel,
    refreshNonce: Int = 0,
    embedded: Boolean = false,
    currentPeriod: StatsViewState.StatsPeriod? = null,
    selectedDate: JavaLocalDate? = null,
    onPeriodChange: ((StatsViewState.StatsPeriod) -> Unit)? = null,
    onDateChange: ((JavaLocalDate) -> Unit)? = null,
) {
    val viewState by remember(viewModel) { viewModel.states() }.collectAsState()

    var internalPeriod by remember { mutableStateOf(StatsViewState.StatsPeriod.WEEK) }
    var internalDate by remember { mutableStateOf(JavaLocalDate.now()) }

    val period = currentPeriod ?: internalPeriod
    val date = selectedDate ?: internalDate

    viewState.Compose(
        refreshNonce = refreshNonce,
        embedded = embedded,
        currentPeriod = period,
        selectedDate = date,
        onPeriodChange = onPeriodChange ?: { internalPeriod = it },
        onDateChange = onDateChange ?: { internalDate = it },
    ) { intent ->
        viewModel.intents().trySend(intent)
    }
}
