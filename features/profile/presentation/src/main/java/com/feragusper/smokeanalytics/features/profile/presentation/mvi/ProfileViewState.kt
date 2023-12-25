package com.feragusper.smokeanalytics.features.profile.presentation.mvi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.profile.presentation.R
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.authentication.presentation.GoogleSignInComponent
import com.feragusper.smokeanalytics.libraries.design.theme.SmokeAnalyticsTheme
import kotlinx.coroutines.launch

data class ProfileViewState(
    internal val displayLoading: Boolean = false,
    internal val currentUserName: String? = null,
) : MVIViewState<ProfileIntent> {

    interface TestTags {
        companion object {
            const val BUTTON_SIGN_OUT = "buttonSignOut"
            const val BUTTON_SIGN_IN = "buttonSignIn"
            const val VIEW_PROGRESS = "viewProgress"
        }
    }

    @Composable
    override fun Compose(intent: (ProfileIntent) -> Unit) {
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
                if (displayLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.testTag(TestTags.VIEW_PROGRESS)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                    )
                    currentUserName?.let { displayName ->
                        Text(
                            text = "Authenticated as $displayName",
                            modifier = Modifier.padding(6.dp)
                        )
                        Button(
                            modifier = Modifier.testTag(TestTags.BUTTON_SIGN_OUT),
                            onClick = { intent(ProfileIntent.SignOut) }
                        ) {
                            Text(text = "Sign out")
                        }
                    } ?: run {
                        val scope = rememberCoroutineScope()
                        GoogleSignInComponent(
                            modifier = Modifier.testTag(TestTags.BUTTON_SIGN_IN),
                            onSignInSuccess = { intent(ProfileIntent.FetchUser) },
                            onSignInError = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("An error has occurred")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun HomeViewPreview() {
    SmokeAnalyticsTheme {
        ProfileViewState().Compose {}
    }
}
