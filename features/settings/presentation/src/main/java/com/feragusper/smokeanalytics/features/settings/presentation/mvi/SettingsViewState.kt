package com.feragusper.smokeanalytics.features.settings.presentation.mvi

import androidx.compose.runtime.Composable
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.compose.SettingsViewState
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme

@CombinedPreviews
@Composable
private fun SettingsLoadingPreview() {
    SmokeAnalyticsTheme {
        SettingsViewState(
            displayLoading = true
        ).Compose {}
    }
}

@CombinedPreviews
@Composable
private fun SettingsLoggedOutViewPreview() {
    SmokeAnalyticsTheme {
        SettingsViewState().Compose {}
    }
}

@CombinedPreviews
@Composable
private fun SettingsLoggedInViewPreview() {
    SmokeAnalyticsTheme {
        SettingsViewState(
            currentEmail = "fernancho@gmail.com"
        ).Compose {}
    }
}
