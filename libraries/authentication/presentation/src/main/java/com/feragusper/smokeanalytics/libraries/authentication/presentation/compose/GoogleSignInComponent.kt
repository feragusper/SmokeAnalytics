package com.feragusper.smokeanalytics.libraries.authentication.presentation.compose

import android.app.Activity
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.libraries.authentication.presentation.BuildConfig
import com.feragusper.smokeanalytics.libraries.authentication.presentation.R
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun GoogleSignInComponent(
    modifier: Modifier = Modifier,
    onSignInSuccess: () -> Unit,
    onSignInError: () -> Unit,
) {
    val activity = LocalContext.current as? Activity
    val coroutine = rememberCoroutineScope()
    val oneTapClient = activity?.let { Identity.getSignInClient(it) }
    val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(BuildConfig.GOOGLE_AUTH_SERVER_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .build(),
        )
        .setAutoSelectEnabled(true)
        .build()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {
            try {
                val credential = oneTapClient?.getSignInCredentialFromIntent(it.data)
                val idToken = credential?.googleIdToken
                if (idToken != null) {
                    Firebase.auth.signInWithCredential(
                        GoogleAuthProvider.getCredential(
                            idToken,
                            null,
                        ),
                    )
                        .addOnCompleteListener(activity) { task ->
                            if (task.isSuccessful) {
                                onSignInSuccess()
                            } else {
                                Log.w("TAG", "signInWithCredential:failure", task.exception)
                            }
                        }
                } else {
                    Log.d("TAG", "Id is null.")
                }
            } catch (e: ApiException) {
                when (e.statusCode) {
                    CommonStatusCodes.CANCELED -> {
                        Log.d("TAG", "One-tap dialog was closed.")
                        // Don't re-prompt the user.
//                            showOneTapUI = false
                    }

                    CommonStatusCodes.NETWORK_ERROR -> Log.d(
                        "TAG",
                        "One-tap encountered a network error.",
                    )

                    else -> Log.d(
                        "TAG",
                        "Couldn't get credential from result." + e.localizedMessage,
                    )
                }
            }
        },
    )
    Button(
        onClick = {
            oneTapClient?.let {
                signIn(
                    coroutine = coroutine,
                    launcher = launcher,
                    signInRequest = signInRequest,
                    oneTapClient = it,
                    onSignInError = onSignInError
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        shape = RoundedCornerShape(6.dp),
    ) {
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.google),
            contentDescription = null,
        )
        Text(
            text = stringResource(id = R.string.auth_sign_in_with_google),
            modifier = Modifier.padding(6.dp),
        )
    }
}

private fun signIn(
    coroutine: CoroutineScope,
    launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    signInRequest: BeginSignInRequest,
    oneTapClient: SignInClient,
    onSignInError: () -> Unit,
) {
    coroutine.launch {
        try {
            launcher.launch(
                IntentSenderRequest.Builder(
                    oneTapClient.beginSignIn(
                        signInRequest,
                    ).await().pendingIntent,
                ).build(),
            )
        } catch (e: Exception) {
            // No saved credentials found. Launch the One Tap sign-up flow, or
            // do nothing and continue presenting the signed-out UI.
            Log.d("LOG", e.message.toString())
            onSignInError()
        }
    }
}

@CombinedPreviews
@Composable
private fun SettingsLoadingPreview() {
    SmokeAnalyticsTheme {
        GoogleSignInComponent(
            onSignInSuccess = { },
            onSignInError = {},
        )
    }
}
