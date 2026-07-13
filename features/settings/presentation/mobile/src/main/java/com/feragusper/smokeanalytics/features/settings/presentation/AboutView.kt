package com.feragusper.smokeanalytics.features.settings.presentation

import android.app.Activity
import androidx.compose.ui.res.stringResource
import com.feragusper.smokeanalytics.features.settings.presentation.R
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
            Text(stringResource(R.string.about_back_to_settings))
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
            body = stringResource(R.string.about_body)
        )

        AboutBlock(
            title = stringResource(R.string.about_actions),
            body = stringResource(R.string.about_share_subtitle),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ActionIconButton(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.about_share),
                    icon = { Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(22.dp)) },
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Smoke Analytics")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Smoke Analytics helps track smokes, streaks and costs. https://github.com/feragusper/SmokeAnalytics"
                            )
                        }
                        context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.about_share_title)))
                    },
                )
                ActionIconButton(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.about_rate),
                    icon = { Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(22.dp)) },
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
                    },
                )
                ActionIconButton(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.about_report),
                    icon = { Icon(Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.size(22.dp)) },
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/feragusper/SmokeAnalytics/issues/new/choose")
                            )
                        )
                    },
                )
                ActionIconButton(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.about_contact),
                    icon = { Icon(Icons.Filled.Mail, contentDescription = null, modifier = Modifier.size(22.dp)) },
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("mailto:feragusper@gmail.com")
                            )
                        )
                    },
                )
            }
        }

        AboutBlock(
            title = stringResource(R.string.about_copyright),
            body = stringResource(R.string.about_copyright_line)
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
                text = stringResource(R.string.about_version),
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
private fun ActionIconButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp, horizontal = 4.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            icon()
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
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
