package com.feragusper.smokeanalytics.features.settings.presentation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AboutSection()

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack,
        ) {
            Text("Back to settings")
        }
    }
}

@Composable
fun AboutSection() {
    val context = LocalContext.current
    val packageName = context.packageName
    val versionName = context.versionName().orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AboutBlock(
            title = "Smoke Analytics",
            body = "A personal smoking journal focused on visibility over guilt. It helps review the day, compare longer trends, estimate cost, and understand where or when smoking tends to cluster."
        )

        AboutBlock(
            title = "Plan",
            body = "Current tier: Free. Premium remains framed as a future upgrade with richer insights and no ads."
        )

        AboutBlock(
            title = "Actions",
            body = "Use the links below to share the product, rate it, or reach support.",
        ) {
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
                            Uri.parse("https://github.com/feragusper/SmokeAnalytics/issues/new/choose")
                        )
                    )
                }
            ) {
                Text("Report bug")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("mailto:feragusper@gmail.com")
                        )
                    )
                }
            ) {
                Text("Contact us")
            }
        }

        AboutBlock(
            title = "Copyright",
            body = "Smoke Analytics © Fernando Perez. All rights reserved."
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Version",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = versionName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun AboutBlock(
    title: String,
    body: String,
    content: @Composable (() -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content?.invoke()
    }
}
