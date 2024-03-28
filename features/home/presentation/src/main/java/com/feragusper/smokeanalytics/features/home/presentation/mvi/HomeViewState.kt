package com.feragusper.smokeanalytics.features.home.presentation.mvi

import androidx.compose.runtime.Composable
import com.feragusper.smokeanalytics.features.home.presentation.mvi.compose.HomeViewState
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke
import java.time.LocalDateTime

@CombinedPreviews
@Composable
private fun HomeViewLoadingPreview() {
    SmokeAnalyticsTheme {
        HomeViewState(
            displayLoading = true,
        ).Compose {}
    }
}

@CombinedPreviews
@Composable
private fun HomeViewSuccessPreview() {
    SmokeAnalyticsTheme {
        HomeViewState(
            smokesPerDay = 10,
            smokesPerWeek = 20,
            smokesPerMonth = 30,
            timeSinceLastCigarette = 1L to 30L,
            latestSmokes = buildList {
                repeat(4) {
                    add(
                        Smoke(
                            id = "123",
                            date = LocalDateTime.now(),
                            timeElapsedSincePreviousSmoke = 1L to 30L
                        )
                    )
                }
            },
        ).Compose {}
    }
}

