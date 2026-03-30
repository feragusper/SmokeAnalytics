package com.feragusper.smokeanalytics.apps.web

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeMapCluster
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeMapPeriod
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.clusterSmokesForMap
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.smokeMapRange
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MapWebStateHolder(
    private val fetchSmokesUseCase: FetchSmokesUseCase,
    private val fetchUserPreferencesUseCase: FetchUserPreferencesUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    var state by mutableStateOf(MapWebUiState())
        private set

    fun onPeriodChange(period: SmokeMapPeriod) {
        state = state.copy(period = period)
        refresh()
    }

    fun onSelectCluster(cluster: SmokeMapCluster) {
        state = state.copy(selectedCluster = cluster)
    }

    fun refresh() {
        val previous = state
        state = previous.copy(
            isLoading = !previous.hasLoadedOnce,
            isRefreshing = previous.hasLoadedOnce,
            error = false,
        )
        scope.launch {
            runCatching {
                val preferences = fetchUserPreferencesUseCase()
                val (start, end) = smokeMapRange(
                    period = previous.period,
                    dayStartHour = preferences.dayStartHour,
                    manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                )
                val clusters = clusterSmokesForMap(fetchSmokesUseCase(start, end), previous.period)
                previous.copy(
                    isLoading = false,
                    isRefreshing = false,
                    hasLoadedOnce = true,
                    preferences = preferences,
                    clusters = clusters,
                    selectedCluster = previous.selectedCluster?.let { current ->
                        clusters.firstOrNull { it.label == current.label } ?: clusters.maxByOrNull { it.count }
                    } ?: clusters.maxByOrNull { it.count },
                    error = false,
                )
            }.getOrElse {
                previous.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = true,
                )
            }.also { state = it }
        }
    }
}

data class MapWebUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val hasLoadedOnce: Boolean = false,
    val error: Boolean = false,
    val period: SmokeMapPeriod = SmokeMapPeriod.Week,
    val preferences: UserPreferences? = null,
    val clusters: List<SmokeMapCluster> = emptyList(),
    val selectedCluster: SmokeMapCluster? = null,
)
