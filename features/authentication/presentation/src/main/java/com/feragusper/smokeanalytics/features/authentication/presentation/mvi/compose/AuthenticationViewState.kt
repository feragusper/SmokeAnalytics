package com.feragusper.smokeanalytics.features.authentication.presentation.mvi.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.authentication.presentation.R
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationIntent
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.GoogleSignInComponent
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import kotlinx.coroutines.launch

/**
 * Describes the state of the history view, including loading indicators, smoke events, errors, and the currently selected date.
 *
 * @property displayLoading Indicates if the loading UI should be shown.
 * @property smokes The list of [Smoke] events to display, or null if not available.
 * @property error An optional error result affecting the current view state.
 * @property selectedDate The currently selected date for displaying smoke events.
 */
data class AuthenticationViewState(
    internal val displayLoading: Boolean = false,
    internal val error: AuthenticationResult.Error? = null,
) : MVIViewState<AuthenticationIntent> {

    @Composable
    override fun Compose(intent: (AuthenticationIntent) -> Unit) {
        val snackbarHostState = remember { SnackbarHostState() }
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { contentPadding ->
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
                        .padding(contentPadding)
                        .padding(horizontal = 16.dp)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(text = stringResource(id = R.string.authentication_sign_in_to_continue))
                    val scope = rememberCoroutineScope()
                    val context = LocalContext.current
                    GoogleSignInComponent(
                        modifier = Modifier.padding(top = 16.dp),
                        onSignInSuccess = { intent(AuthenticationIntent.FetchUser) },
                        onSignInError = {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(com.feragusper.smokeanalytics.libraries.design.R.string.error_general))
                            }
                        },
                    )
                }

                val context = LocalContext.current
                LaunchedEffect(error) {
                    error?.let {
                        when (it) {
                            AuthenticationResult.Error.Generic -> snackbarHostState.showSnackbar(
                                context.getString(
                                    R.string.error_generic
                                )
                            )
                        }
                    }
                }
            }
        }
    }

}

@CombinedPreviews
@Composable
private fun HistoryViewLoadingPreview() {
    SmokeAnalyticsTheme {
        AuthenticationViewState(
            displayLoading = true,
        ).Compose {}
    }
}

@CombinedPreviews
@Composable
private fun AuthenticationViewSuccessPreview() {
    SmokeAnalyticsTheme {
        AuthenticationViewState().Compose {}
    }
}