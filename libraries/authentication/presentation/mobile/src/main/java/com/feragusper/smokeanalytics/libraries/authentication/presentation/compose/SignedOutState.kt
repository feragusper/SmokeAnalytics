package com.feragusper.smokeanalytics.libraries.authentication.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Shared signed-out placeholder used across screens that need a session. It explains what the
 * screen is and what it would show once signed in, then offers the Google sign-in button — so
 * the logged-out experience is coherent everywhere. Rendered as a centered empty state, not a card.
 *
 * @param icon a glyph representing the screen.
 * @param title what the screen is.
 * @param message what it would show if signed in.
 */
@Composable
fun SignedOutState(
    icon: ImageVector,
    title: String,
    message: String,
    onSignInSuccess: () -> Unit,
    onSignInError: (String) -> Unit,
    modifier: Modifier = Modifier,
    signInErrorMessage: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            // Reserve a stable height so the sign-in button lands in the same place on every
            // screen regardless of how many lines the message wraps to.
            minLines = 3,
        )
        signInErrorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        GoogleSignInComponent(
            modifier = Modifier.fillMaxWidth(),
            onSignInSuccess = onSignInSuccess,
            onSignInError = onSignInError,
        )
    }
}
