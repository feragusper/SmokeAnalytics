package com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.ChatbotIntent
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme

/**
 * Represents the state of the Chatbot screen, encapsulating all UI-related data.
 */
data class ChatbotViewState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : MVIViewState<ChatbotIntent> {

    data class Message(
        val text: String,
        val isFromUser: Boolean
    )

    @Composable
    fun Compose(intent: (ChatbotIntent) -> Unit) {
        var input by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            intent(ChatbotIntent.SendInitialMessageWithContext)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Your personal smoke-free coach",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true
            ) {
                items(items = messages.reversed()) { message ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (message.isFromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = if (message.isFromUser) TextAlign.End else TextAlign.Start,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Decile algo al coach...") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Button(
                    onClick = {
                        if (input.isNotBlank()) {
                            intent(ChatbotIntent.SendMessage(input))
                            input = ""
                        }
                    }
                ) {
                    Text("Enviar")
                }
            }
        }
    }
}

/**
 * Preview for Chatbot Screen.
 */
@CombinedPreviews
@Composable
private fun ChatbotViewPreview() {
    SmokeAnalyticsTheme {
        ChatbotViewState().Compose {}
    }
}