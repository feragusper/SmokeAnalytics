package com.feragusper.smokeanalytics.features.profile.presentation.mvi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.profile.presentation.R
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.authentication.presentation.GoogleSignInComponent
import com.feragusper.smokeanalytics.libraries.design.theme.SmokeAnalyticsTheme

data class ProfileViewState(
    internal val displayLoading: Boolean = false,
    internal val currentUserName: String? = null,
) : MVIViewState<ProfileIntent> {

    @Composable
    override fun Compose(intent: (ProfileIntent) -> Unit) {
        Scaffold {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (displayLoading) {
                    CircularProgressIndicator()
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
                            modifier = Modifier.padding(6.dp),
                        )
                        Button(onClick = { intent(ProfileIntent.SignOut) }) {
                            Text(text = "Sign out")
                        }
                    } ?: run {
                        GoogleSignInComponent { intent(ProfileIntent.FetchUser) }
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
