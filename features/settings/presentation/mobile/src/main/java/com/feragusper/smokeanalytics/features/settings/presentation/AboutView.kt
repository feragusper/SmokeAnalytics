package com.feragusper.smokeanalytics.features.settings.presentation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.versionName
import com.google.android.play.core.review.ReviewManagerFactory

@Composable
fun AboutView(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val packageName = context.packageName
    val versionName = context.versionName().orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AboutCard(
            title = "Smoke Analytics",
            body = "Track less, notice more. Smoking history, streaks, costs and location patterns in one place."
        )

        AboutCard(
            title = "Plan",
            body = "Current tier: Free. Premium is defined as a future upgrade with richer insights and no ads."
        )

        AboutCard(title = "Actions") {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Smoke Analytics")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Smoke Analytics helps track smokes, streaks and costs. https://github.com/feragusper/SmokeAnalytics"
                        )
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Smoke Analytics"))
                }
            ) {
                Text("Share app")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val fallbackToStore = {
                        val marketIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$packageName")
                        )
                        val webIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                        )
                        try {
                            context.startActivity(marketIntent)
                        } catch (_: ActivityNotFoundException) {
                            context.startActivity(webIntent)
                        }
                    }

                    val activity = context as? Activity
                    if (activity == null) {
                        fallbackToStore()
                    } else {
                        val manager = ReviewManagerFactory.create(context)
                        manager.requestReviewFlow()
                            .addOnSuccessListener { reviewInfo ->
                                manager.launchReviewFlow(activity, reviewInfo)
                                    .addOnFailureListener { fallbackToStore() }
                            }
                            .addOnFailureListener { fallbackToStore() }
                    }
                }
            ) {
                Text("Rate app")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/feragusper/SmokeAnalytics")
                        )
                    )
                }
            ) {
                Text("Open GitHub")
            }
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = versionName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack,
        ) {
            Text("Back to settings")
        }
    }
}

@Composable
private fun AboutCard(
    title: String,
    body: String? = null,
    content: @Composable (() -> Unit)? = null,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            body?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content?.invoke()
        }
    }
}
