package com.feragusper.smokeanalytics.libraries.authentication.presentation.compose

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.NoCredentialException
import com.feragusper.smokeanalytics.libraries.authentication.presentation.BuildConfig
import com.feragusper.smokeanalytics.libraries.authentication.presentation.R
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.security.MessageDigest
import java.util.UUID

/**
 * Composable that provides a UI for Google Sign-In with Firebase Authentication
 * using the new Credential Manager API. Handles user sign-in and error states.
 *
 * @param modifier Modifier for the button.
 * @param onSignInSuccess Lambda invoked on successful sign-in.
 * @param onSignInError Lambda invoked when sign-in fails.
 */
@Composable
fun GoogleSignInComponent(
    modifier: Modifier = Modifier,
    onSignInSuccess: () -> Unit,
    onSignInError: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Launcher to start the account addition if no credentials found
    val startAddAccountIntentLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            // Reattempt sign-in if account is added successfully
            doGoogleSignIn(coroutineScope, context, null, onSignInSuccess, onSignInError)
        }

    // Button for Google Sign-In
    Button(
        onClick = {
            doGoogleSignIn(
                coroutineScope,
                context,
                startAddAccountIntentLauncher,
                onSignInSuccess,
                onSignInError
            )
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
 * Handles the Google Sign-In process using Credential Manager.
 *
 * @param coroutineScope The CoroutineScope to launch the asynchronous tasks.
 * @param context The context to initialize the Credential Manager.
 * @param startAddAccountIntentLauncher Launcher to start the add account flow if necessary.
 * @param onSignInSuccess Callback when sign-in is successful.
 * @param onSignInError Callback when sign-in fails.
 */
private fun doGoogleSignIn(
    coroutineScope: CoroutineScope,
    context: Context,
    startAddAccountIntentLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>?,
    onSignInSuccess: () -> Unit,
    onSignInError: () -> Unit
) {
    val credentialManager = CredentialManager.create(context)

    // Create Google ID Option for sign-in
    val googleSignInOption = GetGoogleIdOption.Builder()
        .setServerClientId(BuildConfig.GOOGLE_AUTH_SERVER_CLIENT_ID)
        .setFilterByAuthorizedAccounts(false) // Set to true to use only authorized accounts
        .setAutoSelectEnabled(true) // Auto sign-in for previously signed-in users
        .setNonce(generateNonce()) // Generate nonce to enhance security
        .build()

    val googleSignInRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleSignInOption)
        .build()

    // Launch sign-in request using Credential Manager
    coroutineScope.launch {
        try {
            val result = credentialManager.getCredential(
                request = googleSignInRequest,
                context = context
            )
            handleSignIn(result, onSignInSuccess, onSignInError)
        } catch (e: NoCredentialException) {
            Timber.e(e, "No credentials found")
            // Prompt user to add Google account if no credentials are available
            startAddAccountIntentLauncher?.launch(getAddGoogleAccountIntent())
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during sign-in")
            onSignInError()
        }
    }
}

/**
 * Handles the sign-in result. If successful, authenticates with Firebase.
 *
 * @param result The result of the credential request.
 * @param onSignInSuccess Callback when sign-in is successful.
 * @param onSignInError Callback when sign-in fails.
 */
private suspend fun handleSignIn(
    result: GetCredentialResponse,
    onSignInSuccess: () -> Unit,
    onSignInError: () -> Unit
) {
    val credential = result.credential

    when (credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken

                    // Use the ID token to authenticate with Firebase
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    Firebase.auth.signInWithCredential(authCredential).await()

                    // Invoke success callback
                    onSignInSuccess()
                } catch (e: GoogleIdTokenParsingException) {
                    Timber.e(e, "Invalid Google ID Token")
                    onSignInError()
                } catch (e: Exception) {
                    Timber.e(e, "Unexpected error during sign-in")
                    onSignInError()
                }
            } else {
                Timber.e("Unexpected credential type")
                onSignInError()
            }
        }

        else -> {
            Timber.e("Credential is not of the expected type")
            onSignInError()
        }
    }
}

/**
 * Generates a nonce for securing the Google sign-in request.
 */
private fun generateNonce(): String {
    val rawNonce = UUID.randomUUID().toString()
    val bytes = rawNonce.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.joinToString("") { "%02x".format(it) }
}

/**
 * Provides the intent to add a Google account.
 */
private fun getAddGoogleAccountIntent(): Intent {
    val intent = Intent(Settings.ACTION_ADD_ACCOUNT)
    intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
    return intent
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