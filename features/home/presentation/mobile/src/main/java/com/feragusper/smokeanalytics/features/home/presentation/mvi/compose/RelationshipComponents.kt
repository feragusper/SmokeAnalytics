package com.feragusper.smokeanalytics.features.home.presentation.mvi.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
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
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeTrigger
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.TriggerOption
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.normalizedTag

/**
 * Home card reminding the user that some smokes (logged from the watch, or whose prompt
 * was dismissed) still have no trigger. Tapping the CTA opens the prompt for the next one.
 */
@Composable
internal fun RelationshipReminderCard(
    pendingCount: Int,
    onAdd: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "What were these about?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (pendingCount == 1) {
                    "1 cigarette still needs a trigger"
                } else {
                    "$pendingCount cigarettes still need a trigger"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add trigger", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Bottom sheet asking what a smoke was related to. Multi-select chips plus an "Other"
 * free-text field; the user can save, declare "no relation", or dismiss (stays untracked).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RelationshipPromptSheet(
    availableTriggers: List<TriggerOption>,
    onSave: (tags: Set<String>) -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Chips come from the catalog (defaults + custom). The user can also type an ad-hoc
    // tag, which is added to the chip row for this smoke.
    val catalog = remember(availableTriggers) {
        if (availableTriggers.isEmpty()) SmokeTrigger.defaultOptions() else availableTriggers
    }
    var adHoc by remember { mutableStateOf(listOf<TriggerOption>()) }
    var selectedKeys by remember { mutableStateOf(emptySet<String>()) }
    var draft by remember { mutableStateOf("") }

    val options = catalog + adHoc.filter { extra -> catalog.none { it.key == extra.key } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "What was it related to?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Tag what triggered this cigarette, or skip if it was nothing in particular.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
                        label = { Text(option.label) },
                    )
                }
            }
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Add a tag") },
                placeholder = { Text("Type and press Add…") },
                singleLine = true,
                trailingIcon = {
                    val key = draft.normalizedTag()
                    if (key != null) {
                        TextButton(onClick = {
                            adHoc = adHoc + TriggerOption(key = key, label = key, isCustom = true)
                            selectedKeys = selectedKeys + key
                            draft = ""
                        }) { Text("Add") }
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
                    Text("No relation")
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
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@CombinedPreviews
@Composable
private fun RelationshipReminderCardPreview() {
    SmokeAnalyticsTheme {
        RelationshipReminderCard(pendingCount = 3, onAdd = {})
    }
}
