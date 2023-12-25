package com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.feragusper.smokeanalytics.features.home.domain.Smoke
import com.feragusper.smokeanalytics.features.home.presentation.R
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeViewState.TestTags.Companion.BUTTON_ADD_SMOKE
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.design.theme.SmokeAnalyticsTheme
import java.util.Date

data class HomeViewState(
    internal val displayLoading: Boolean = false,
    internal val displaySmokeAddedSuccess: Boolean = false,
    internal val smokeAddError: HomeResult.AddSmokeError? = null,
    internal val smokesPerDay: Int? = null,
    internal val smokesPerWeek: Int? = null,
    internal val smokesPerMonth: Int? = null,
    internal val latestSmokes: List<Smoke>? = null,
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
            if (displayLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
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
                    smokesPerDay?.let { smokesPerDay ->
                        Text(
                            text = stringResource(R.string.home_label_per_day),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = smokesPerDay.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                    }
                    smokesPerWeek?.let { smokesPerWeek ->
                        Text(
                            text = stringResource(R.string.home_label_per_week),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = smokesPerWeek.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                    }
                    smokesPerMonth?.let { smokesPerMonth ->
                        Text(
                            text = stringResource(R.string.home_label_per_month),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = smokesPerMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                    }
                    LazyColumn {
                        items(latestSmokes ?: emptyList()) { smoke ->
                            Text(
                                text = smoke.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Button(
                        modifier = Modifier.testTag(BUTTON_ADD_SMOKE),
                        onClick = { intent(HomeIntent.AddSmoke) }
                    ) {
                        Text(text = stringResource(R.string.home_button_add_smoke))
                    }
                }
            }
        }

        val context = LocalContext.current
        LaunchedEffect(smokeAddError) {
            smokeAddError?.let {
                when (it) {
                    HomeResult.AddSmokeError.Generic -> snackbarHostState.showSnackbar(
                        context.getString(
                            R.string.error_generic
                        )
                    )

                    HomeResult.AddSmokeError.NotLoggedIn -> Toast.makeText(
                        context,
                        context.getString(R.string.error_not_logged_in),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}

@Preview
@Composable
private fun HomeViewLoadingPreview() {
    SmokeAnalyticsTheme {
        HomeViewState(
            displayLoading = true,
        ).Compose {}
    }
}

@Preview
@Composable
private fun HomeViewSuccessPreview() {
    SmokeAnalyticsTheme {
        HomeViewState(
            smokesPerDay = 10,
            smokesPerWeek = 20,
            smokesPerMonth = 30,
            latestSmokes = listOf(
                Smoke(Date()),
                Smoke(Date()),
                Smoke(Date()),
                Smoke(Date()),
                Smoke(Date()),
            ),
        ).Compose {}
    }
}

