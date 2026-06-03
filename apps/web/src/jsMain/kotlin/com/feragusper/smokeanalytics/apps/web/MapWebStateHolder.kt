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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

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

    fun refresh(
        period: SmokeMapPeriod = state.period,
        selectedDate: LocalDate = state.selectedDate,
    ) {
        val previous = state
        state = previous.copy(
            isLoading = !previous.hasLoadedOnce,
            isRefreshing = previous.hasLoadedOnce,
            error = false,
            period = period,
            selectedDate = selectedDate,
        )
        scope.launch {
            runCatching {
                val preferences = fetchUserPreferencesUseCase()
                val (start, end) = smokeMapRange(
                    period = period,
                    dayStartHour = preferences.dayStartHour,
                    manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                    selectedDate = selectedDate,
                )
                val clusters = clusterSmokesForMap(fetchSmokesUseCase(start, end), period)
                previous.copy(
                    isLoading = false,
                    isRefreshing = false,
                    hasLoadedOnce = true,
                    preferences = preferences,
                    period = period,
                    selectedDate = selectedDate,
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
                    period = period,
                    selectedDate = selectedDate,
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
    val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val preferences: UserPreferences? = null,
    val clusters: List<SmokeMapCluster> = emptyList(),
    val selectedCluster: SmokeMapCluster? = null,
)
