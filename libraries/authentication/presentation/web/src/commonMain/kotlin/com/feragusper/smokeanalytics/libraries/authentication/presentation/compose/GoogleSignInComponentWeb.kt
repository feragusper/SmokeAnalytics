package com.feragusper.smokeanalytics.libraries.authentication.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import dev.gitlive.firebase.auth.externals.GoogleAuthProvider
import dev.gitlive.firebase.auth.externals.getAuth
import dev.gitlive.firebase.auth.externals.signInWithPopup
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.await
import kotlinx.coroutines.launch

/**
 * Represents a component for signing in with Google.
 *
 * @param onSignInSuccess The callback to invoke when the sign in is successful.
 * @param onSignInError The callback to invoke when the sign in fails.
 */
@Composable
fun GoogleSignInComponentWeb(
    onSignInSuccess: () -> Unit,
    onSignInError: (Throwable) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    PrimaryButton(
        text = if (loading) "Signing in..." else "Continue with Google",
        enabled = !loading,
        onClick = {
            scope.launch(start = CoroutineStart.UNDISPATCHED) {
                loading = true
                try {
                    runCatching {
                        val provider = GoogleAuthProvider()
                        signInWithPopup(getAuth(), provider).await()
                    }.onSuccess {
                        onSignInSuccess()
                    }.onFailure { t ->
                        val code = (t.asDynamic().code as? String)
                        val msg = (t.asDynamic().message as? String) ?: t.message
                        onSignInError(
                            RuntimeException(
                                "${code ?: "unknown"}: ${msg ?: "Unknown"}",
                                t
                            )
                        )
                    }
                } finally {
                    loading = false
                }
            }
        }
    )
}
