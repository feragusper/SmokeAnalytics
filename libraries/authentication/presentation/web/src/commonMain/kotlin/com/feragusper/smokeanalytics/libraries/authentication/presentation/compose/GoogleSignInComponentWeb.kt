package com.feragusper.smokeanalytics.libraries.authentication.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.auth.externals.GoogleAuthProvider
import dev.gitlive.firebase.auth.externals.signInWithPopup
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text

@Composable
fun GoogleSignInComponentWeb(
    onSignInSuccess: () -> Unit,
    onSignInError: (Throwable) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    Button(
        attrs = {
            if (loading) disabled()
            onClick {
                scope.launch(start = CoroutineStart.UNDISPATCHED) {
                    loading = true
                    try {
                        runCatching {
                            val auth = Firebase.auth
                            val provider = GoogleAuthProvider()
                            signInWithPopup(auth.js, provider).await()
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
        }
    ) {
        Text(if (loading) "Signing in..." else "Sign in with Google")
    }
}