package com.feragusper.smokeanalytics.features.devtools.presentation.mvi.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.devtools.presentation.R
import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsIntent
import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.versionName
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme

/**
 * The state of the DevTools view, containing all necessary data for rendering the UI.
 *
 * @property displayLoading Indicates if a loading indicator should be displayed.
 * @property currentUser The current user's information if logged in.
 */
data class DevToolsViewState(
    internal val displayLoading: Boolean = false,
    internal val currentUser: User? = null,
) : MVIViewState<DevToolsIntent> {

    data class User(val email: String?, val id: String)

    @Composable
    override fun Compose(intent: (DevToolsIntent) -> Unit) {
        val snackbarHostState = remember { SnackbarHostState() }
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .padding(vertical = 16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (displayLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = stringResource(R.string.devtools_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    currentUser?.let { user ->
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = null
                            )
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                user.email?.let { email ->
                                    Text(
                                        text = stringResource(id = R.string.devtools_email),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    text = stringResource(id = R.string.devtools_id),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = user.id,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        HorizontalDivider()
                    } ?: run {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = stringResource(id = R.string.devtools_logged_out),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.weight(1F))
                    LocalContext.current.versionName()?.let { versionName ->
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = versionName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@CombinedPreviews
@Composable
private fun DevToolsLoadingPreview() {
    SmokeAnalyticsTheme {
        DevToolsViewState(
            displayLoading = true
        ).Compose {}
    }
}

@CombinedPreviews
@Composable
private fun DevToolsLoggedOutViewPreview() {
    SmokeAnalyticsTheme {
        DevToolsViewState().Compose {}
    }
}

@CombinedPreviews
@Composable
private fun DevToolsLoggedInViewPreview() {
    SmokeAnalyticsTheme {
        DevToolsViewState(
            currentUser = DevToolsViewState.User(
                id = "123",
                email = "fernancho@gmail.com"
            )
        ).Compose {}
    }
}