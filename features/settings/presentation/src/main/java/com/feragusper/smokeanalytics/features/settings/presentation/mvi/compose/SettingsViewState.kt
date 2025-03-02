package com.feragusper.smokeanalytics.features.settings.presentation.mvi.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.settings.presentation.R
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.versionName
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.GoogleSignInComponent
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import kotlinx.coroutines.launch

/**
 * Represents the state of the Settings screen, encapsulating UI-related data.
 *
 * @property displayLoading Indicates if a loading indicator should be displayed.
 * @property currentEmail The email of the currently logged-in user, if available.
 */
data class SettingsViewState(
    internal val displayLoading: Boolean = false,
    internal val currentEmail: String? = null,
) : MVIViewState<SettingsIntent> {

    interface TestTags {
        companion object {
            const val BUTTON_SIGN_OUT = "buttonSignOut"
            const val BUTTON_SIGN_IN = "buttonSignIn"
            const val VIEW_PROGRESS = "viewProgress"
        }
    }

    @Composable
    override fun Compose(intent: (SettingsIntent) -> Unit) {
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
                when {
                    displayLoading -> LoadingView()
                    currentEmail != null -> LoggedInView(
                        currentEmail = currentEmail,
                        onSignOut = { intent(SettingsIntent.SignOut) }
                    )

                    else -> LoggedOutView(
                        onSignInSuccess = { intent(SettingsIntent.FetchUser) },
                        snackbarHostState = snackbarHostState
                    )
                }
                AppVersionFooter()
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.testTag(SettingsViewState.TestTags.VIEW_PROGRESS)
        )
    }
}

@Composable
private fun LoggedInView(
    currentEmail: String,
    onSignOut: () -> Unit
) {
    Text(
        modifier = Modifier.padding(16.dp),
        text = stringResource(R.string.settings_title),
        style = MaterialTheme.typography.titleLarge,
    )
    UserInfo(email = currentEmail)
    HorizontalDivider()
    Button(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .testTag(SettingsViewState.TestTags.BUTTON_SIGN_OUT),
        onClick = onSignOut
    ) {
        Text(text = stringResource(id = R.string.settings_logout))
    }
}

@Composable
private fun UserInfo(email: String) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_person),
            contentDescription = null
        )
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = stringResource(id = R.string.settings_email),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LoggedOutView(
    onSignInSuccess: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    GoogleSignInComponent(
        modifier = Modifier.testTag(SettingsViewState.TestTags.BUTTON_SIGN_IN),
        onSignInSuccess = onSignInSuccess,
        onSignInError = {
            scope.launch {
                snackbarHostState.showSnackbar(
                    context.getString(com.feragusper.smokeanalytics.libraries.design.R.string.error_general)
                )
            }
        },
    )
}

@Composable
private fun AppVersionFooter() {
    // Place this inside a Column or Row to inherit the scope and access .weight()
    Column(modifier = Modifier.fillMaxSize()) {
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
