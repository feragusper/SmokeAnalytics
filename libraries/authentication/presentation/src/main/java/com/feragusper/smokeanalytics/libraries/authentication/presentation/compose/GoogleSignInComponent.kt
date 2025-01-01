package com.feragusper.smokeanalytics.libraries.authentication.presentation.compose

import android.app.Activity
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
import timber.log.Timber

/**
 * A composable function that provides a UI component for Google Sign-In. It handles the sign-in process using Firebase Authentication
 * and Google Identity Services.
 *
 * @param modifier The [Modifier] to be applied to the button component.
 * @param onSignInSuccess A lambda to be invoked upon successful sign-in.
 * @param onSignInError A lambda to be invoked upon sign-in failure.
 */
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
                                onSignInError()
                                Timber.w("signInWithCredential:failure", task.exception)
                            }
                        }
                } else {
                    Timber.d("Id is null.")
                }
            } catch (e: ApiException) {
                when (e.statusCode) {
                    CommonStatusCodes.CANCELED -> {
                        Timber.d("One-tap dialog was closed.")
                        // Don't re-prompt the user.
//                            showOneTapUI = false
                    }

                    CommonStatusCodes.NETWORK_ERROR -> {
                        onSignInError()
                        Timber.d(
                            "One-tap encountered a network error.",
                        )
                    }

                    else -> {
                        onSignInError()
                        Timber.d(
                            "Couldn't get credential from result." + e.localizedMessage,
                        )
                    }
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

/**
 * Initiates the sign-in process by launching the Google Sign-In Intent. On success or failure, appropriate callbacks are invoked.
 *
 * @param coroutine The [CoroutineScope] to launch asynchronous tasks.
 * @param launcher The [ManagedActivityResultLauncher] to handle the result of the sign-in intent.
 * @param signInRequest The [BeginSignInRequest] configuration for the Google Sign-In.
 * @param oneTapClient The [SignInClient] instance for managing sign-in requests.
 * @param onSignInError A lambda to be invoked upon sign-in failure.
 */
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
            Timber.e(e.message.toString())
            onSignInError()
        }
    }
}

@CombinedPreviews
@Composable
private fun GoogleSignInComponentPreview() {
    SmokeAnalyticsTheme {
        GoogleSignInComponent(
            onSignInSuccess = { },
            onSignInError = {},
        )
    }
}
