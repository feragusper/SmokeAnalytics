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

/** Human-readable label for a trigger chip. */
internal fun SmokeTrigger.label(): String = when (this) {
    SmokeTrigger.COFFEE -> "Coffee"
    SmokeTrigger.ALCOHOL -> "Alcohol"
    SmokeTrigger.BOREDOM -> "Boredom"
    SmokeTrigger.ANXIETY -> "Anxiety"
    SmokeTrigger.STRESS -> "Stress"
    SmokeTrigger.AFTER_MEAL -> "After a meal"
    SmokeTrigger.SOCIAL -> "Social"
    SmokeTrigger.BREAK -> "Break"
    SmokeTrigger.DRIVING -> "Driving"
    SmokeTrigger.PHONE -> "Phone"
}

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
    onSave: (triggers: Set<SmokeTrigger>, note: String?) -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selected by remember { mutableStateOf(emptySet<SmokeTrigger>()) }
    var note by remember { mutableStateOf("") }

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
                SmokeTrigger.entries.forEach { trigger ->
                    FilterChip(
                        selected = trigger in selected,
                        onClick = {
                            selected = if (trigger in selected) selected - trigger else selected + trigger
                        },
                        label = { Text(trigger.label()) },
                    )
                }
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Other") },
                placeholder = { Text("Add your own…") },
                singleLine = true,
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
                    onClick = { onSave(selected, note.trim().takeIf { it.isNotEmpty() }) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    enabled = selected.isNotEmpty() || note.isNotBlank(),
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
