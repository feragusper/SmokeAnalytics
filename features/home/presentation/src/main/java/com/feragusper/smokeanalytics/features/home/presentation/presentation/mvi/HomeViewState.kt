package com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.feragusper.smokeanalytics.features.home.presentation.R
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeViewState.TestTags.Companion.BUTTON_ADD_SMOKE
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.design.theme.SmokeAnalyticsTheme

data class HomeViewState(
    internal val displayLoading: Boolean = false,
    internal val displaySmokeAddedSuccess: Boolean = false,
    internal val displaySmokeAddedError: Boolean = false,
) : MVIViewState<HomeIntent> {
    interface TestTags {
        companion object {
            const val BUTTON_ADD_SMOKE = "buttonAddSmoke"
        }
    }

    @Composable
    override fun Compose(intent: (HomeIntent) -> Unit) {
        val snackbarHostState = remember { SnackbarHostState() }
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                )
                Button(
                    modifier = Modifier.testTag(BUTTON_ADD_SMOKE),
                    onClick = { intent(HomeIntent.AddSmoke) }
                ) {
                    Text(text = stringResource(R.string.home_button_add_smoke))
                }
            }
        }

        LaunchedEffect(displaySmokeAddedError) {
            if (displaySmokeAddedError) {
                snackbarHostState.showSnackbar("An error has occurred")
            }
        }
    }

}

@Preview
@Composable
private fun HomeViewPreview() {
    SmokeAnalyticsTheme {
        HomeViewState().Compose {}
    }
}
