package com.feragusper.smokeanalytics.features.history.presentation.mvi

import androidx.compose.runtime.Composable
import com.feragusper.smokeanalytics.features.history.presentation.mvi.compose.HistoryViewState
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke
import java.time.LocalDateTime

@CombinedPreviews
@Composable
private fun HomeViewLoadingPreview() {
    SmokeAnalyticsTheme {
        HistoryViewState(
            displayLoading = true,
        ).Compose {}
    }
}

@CombinedPreviews
@Composable
private fun HomeViewSuccessPreview() {
    SmokeAnalyticsTheme {
        HistoryViewState(
            smokes =
            buildList {
                repeat(4) {
                    add(
                        Smoke(
                            id = "123",
                            date = LocalDateTime.now(),
                            timeElapsedSincePreviousSmoke = 1L to 30L
                        )
                    )
                }
            }
        ).Compose {}
    }
}
