package com.feragusper.smokeanalytics.features.authentication.presentation.mvi.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.authentication.presentation.R
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationIntent
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.GoogleSignInComponent
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import kotlinx.coroutines.launch

data class AuthenticationViewState(
    internal val displayLoading: Boolean = false,
    internal val error: AuthenticationResult.Error? = null,
) : MVIViewState<AuthenticationIntent> {

    @Composable
    fun Compose(intent: (AuthenticationIntent) -> Unit) {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { contentPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .padding(contentPadding)
                        .padding(horizontal = 16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    item { AuthHeroCard() }
                    item {
                        AuthEntryCard(
                            onRefresh = { intent(AuthenticationIntent.FetchUser) },
                            onSignInError = { message ->
                                scope.launch { snackbarHostState.showSnackbar(message) }
                            },
                        )
                    }
                    item { AuthHighlightsCard() }
                    item { SupportCard() }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }

                if (displayLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            tonalElevation = 4.dp,
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "Checking session",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                    }
                }
            }

            val message = stringResource(R.string.error_generic)
            LaunchedEffect(error) {
                error?.let {
                    when (it) {
                        AuthenticationResult.Error.Generic -> snackbarHostState.showSnackbar(message)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthHeroCard() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    text = "Auth",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Text(
                text = "Bring the full product back into sync.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Sign in with Google to recover history, restore preferences, and keep the coach grounded in your recent smoking context.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AuthEntryCard(
    onRefresh: () -> Unit,
    onSignInError: (String) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Continue with Google",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Use the same account to preserve your archive, analytics framing, and settings across devices.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            GoogleSignInComponent(
                modifier = Modifier.padding(top = 4.dp),
                onSignInSuccess = onRefresh,
                onSignInError = onSignInError,
            )
        }
    }
}

@Composable
private fun AuthHighlightsCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "What comes back with your session",
                style = MaterialTheme.typography.titleMedium,
            )
            AuthHighlightRow(
                title = "History",
                body = "Edits, day buckets, and archive browsing stay tied to one account.",
            )
            AuthHighlightRow(
                title = "Routine",
                body = "Pack price, day-start hour, and map preferences stay stable across devices.",
            )
            AuthHighlightRow(
                title = "Coach",
                body = "Insights keep their context instead of starting from a blank session.",
            )
        }
    }
}

@Composable
private fun AuthHighlightRow(
    title: String,
    body: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SupportCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "This keeps the same product scope",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Logging, analytics, map insights, archive editing, and coaching all stay intact. This screen only restores the account context around them.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@CombinedPreviews
@Composable
private fun AuthenticationViewLoadingPreview() {
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
