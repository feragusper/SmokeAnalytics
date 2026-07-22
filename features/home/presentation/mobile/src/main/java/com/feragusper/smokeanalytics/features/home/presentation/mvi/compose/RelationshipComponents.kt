package com.feragusper.smokeanalytics.features.home.presentation.mvi.compose

import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsScreen
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTarget
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTracker
import org.koin.compose.koinInject
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.feragusper.smokeanalytics.features.home.presentation.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.TriggerOption
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.normalizedTag
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime

/**
 * Home card reminding the user that some smokes (logged from the watch, or whose prompt
 * was dismissed) still have no trigger. Each carries its own date/time so the user can recall
 * what it was about and tag them one at a time — not all at once.
 */
@Composable
internal fun RelationshipReminderCard(
    pending: List<PendingTriggerSmoke>,
    onOpen: (smokeId: String) -> Unit,
) {
    val shown = pending.take(MAX_PENDING_SHOWN)
    val analytics = koinInject<AnalyticsTracker>()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.home_what_were_these),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = pluralStringResource(R.plurals.home_pending_needs_trigger, pending.size, pending.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            shown.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    TextButton(onClick = {
                        analytics.buttonTap(AnalyticsScreen.HOME, AnalyticsTarget.RELATIONSHIP_OPEN)
                        onOpen(item.id)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.home_add_trigger))
                    }
                }
            }
            if (pending.size > shown.size) {
                Text(
                    text = pluralStringResource(R.plurals.home_more_count, pending.size - shown.size, pending.size - shown.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** A pending (untracked) smoke shown in the reminder card: its id and a date/time label. */
internal data class PendingTriggerSmoke(
    val id: String,
    val label: String,
)

private const val MAX_PENDING_SHOWN = 8

/** Human label for a pending smoke in the user's language, e.g. "Mon Jun 24 · 14:30" / "lun jun 24 · 14:30". */
internal fun Instant.toPendingTriggerLabel(
    locale: java.util.Locale,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val dt = toLocalDateTime(timeZone)
    val weekday = java.time.DayOfWeek.of(dt.dayOfWeek.isoDayNumber)
        .getDisplayName(java.time.format.TextStyle.SHORT, locale)
    val month = java.time.Month.of(dt.monthNumber)
        .getDisplayName(java.time.format.TextStyle.SHORT, locale)
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')
    return "$weekday $month ${dt.dayOfMonth} · $hour:$minute"
}

/**
 * Bottom sheet asking what a smoke was related to. Multi-select chips plus an "Other"
 * free-text field; the user can save, declare "no relation", or dismiss (stays untracked).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RelationshipPromptSheet(
    availableTriggers: List<TriggerOption>?,
    dateLabel: String? = null,
    onSave: (tags: Set<String>) -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Freeze the catalog the moment it's loaded (keyed on loaded-ness, not the list) so
    // chips don't pop in mid-dialog when a background refresh lands; while it's still
    // null the sheet shows a loading indicator instead of a partial default list.
    val catalog = remember(availableTriggers != null) { availableTriggers }
    var adHoc by remember { mutableStateOf(listOf<TriggerOption>()) }
    var selectedKeys by remember { mutableStateOf(emptySet<String>()) }
    var draft by remember { mutableStateOf("") }

    val options = catalog?.plus(adHoc.filter { extra -> catalog.none { it.key == extra.key } })

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // The sheet's dialog window already resizes when the IME opens — do NOT add
                // ime padding here too (double-applying pushed the content out of the sheet
                // entirely). Scrolling is enough: the sheet shrinks, content scrolls, and the
                // focused text field is auto-scrolled into view.
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.home_what_related),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            dateLabel?.let {
                Text(
                    text = stringResource(R.string.home_logged, it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.home_tag_what_triggered),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (options == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    options.forEach { option ->
                        FilterChip(
                            selected = option.key in selectedKeys,
                            onClick = {
                                selectedKeys = if (option.key in selectedKeys) {
                                    selectedKeys - option.key
                                } else {
                                    selectedKeys + option.key
                                }
                            },
                            label = { Text(option.display) },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.home_add_a_tag)) },
                placeholder = { Text(stringResource(R.string.home_type_and_add)) },
                singleLine = true,
                trailingIcon = {
                    val key = draft.normalizedTag()
                    if (key != null) {
                        TextButton(onClick = {
                            adHoc = adHoc + TriggerOption(key = key, label = key, isCustom = true)
                            selectedKeys = selectedKeys + key
                            draft = ""
                        }) { Text(stringResource(R.string.home_add)) }
                    }
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(stringResource(R.string.home_no_relation))
                }
                Button(
                    onClick = {
                        val tags = selectedKeys + listOfNotNull(draft.normalizedTag())
                        onSave(tags)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    enabled = selectedKeys.isNotEmpty() || draft.normalizedTag() != null,
                ) {
                    Text(stringResource(R.string.home_save), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@CombinedPreviews
@Composable
private fun RelationshipReminderCardPreview() {
    SmokeAnalyticsTheme {
        RelationshipReminderCard(
            pending = listOf(
                PendingTriggerSmoke("1", "Mon Jun 24 · 14:30"),
                PendingTriggerSmoke("2", "Mon Jun 24 · 09:05"),
            ),
            onOpen = {},
        )
    }
}
