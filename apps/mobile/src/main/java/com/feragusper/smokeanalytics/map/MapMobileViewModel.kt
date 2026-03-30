package com.feragusper.smokeanalytics.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeMapCluster
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeMapPeriod
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.clusterSmokesForMap
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.smokeMapRange
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapMobileViewModel @Inject constructor(
    private val fetchSmokesUseCase: FetchSmokesUseCase,
    private val fetchUserPreferencesUseCase: FetchUserPreferencesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(MapMobileState())
    val state: StateFlow<MapMobileState> = _state.asStateFlow()

    fun onPeriodChange(period: SmokeMapPeriod) {
        _state.value = _state.value.copy(period = period)
        refresh()
    }

    fun onScreenVisible() {
        if (_state.value.hasLoadedOnce) {
            refresh(isRefresh = true)
        } else {
            refresh()
        }
    }

    fun onSelectCluster(cluster: SmokeMapCluster) {
        _state.value = _state.value.copy(selectedCluster = cluster)
    }

    fun refresh(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val previous = _state.value
            _state.value = previous.copy(
                isLoading = !previous.hasLoadedOnce,
                isRefreshing = isRefresh && previous.hasLoadedOnce,
                error = false,
            )
            runCatching {
                val preferences = fetchUserPreferencesUseCase()
                val (start, end) = smokeMapRange(
                    period = previous.period,
                    dayStartHour = preferences.dayStartHour,
                    manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                )
                val smokes = fetchSmokesUseCase(start, end)
                val clusters = clusterSmokesForMap(smokes, previous.period)
                previous.copy(
                    isLoading = false,
                    isRefreshing = false,
                    hasLoadedOnce = true,
                    preferences = preferences,
                    clusters = clusters,
                    selectedCluster = previous.selectedCluster?.let { current ->
                        clusters.firstOrNull { it.label == current.label } ?: clusters.firstOrNull()
                    } ?: clusters.firstOrNull(),
                    error = false,
                )
            }.getOrElse {
                previous.copy(isLoading = false, isRefreshing = false, error = true)
            }.also { _state.value = it }
        }
    }
}

data class MapMobileState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val hasLoadedOnce: Boolean = false,
    val error: Boolean = false,
    val period: SmokeMapPeriod = SmokeMapPeriod.Week,
    val preferences: UserPreferences? = null,
    val clusters: List<SmokeMapCluster> = emptyList(),
    val selectedCluster: SmokeMapCluster? = null,
)
