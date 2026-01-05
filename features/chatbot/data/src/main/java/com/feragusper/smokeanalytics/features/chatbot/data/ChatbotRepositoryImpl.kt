package com.feragusper.smokeanalytics.features.chatbot.data

import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotRepository
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.google.ai.client.generativeai.GenerativeModel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Singleton
class ChatbotRepositoryImpl @Inject constructor(
    private val gemini: GenerativeModel,
) : ChatbotRepository {

    private val timeZone: TimeZone = TimeZone.currentSystemDefault()

    override suspend fun sendMessage(message: String): String {
        return runCatching {
            gemini.generateContent(message).text
        }.getOrElse { throwable ->
            throwable.printStackTrace()
            null
        } ?: "No response from the model."
    }

    override suspend fun sendInitialMessageWithContext(
        name: String,
        recentSmokes: List<Smoke>,
    ): String {
        val today = Clock.System.now().toLocalDateTime(timeZone).date

        val todayCount = recentSmokes.count { smoke ->
            smoke.date.toLocalDateTime(timeZone).date == today
        }

        val total = recentSmokes.size

        val lastDate = recentSmokes
            .firstOrNull()
            ?.date
            ?.toLocalDateTime(timeZone)
            ?.toString()
            ?: "Not recorded"

        val prompt = buildPrompt(
            name = name,
            today = todayCount,
            total = total,
            lastDate = lastDate,
        )

        return runCatching {
            gemini.generateContent(prompt).text
        }.getOrElse { throwable ->
            throwable.printStackTrace()
            null
        } ?: "No response from the model."
    }

    private fun buildPrompt(
        name: String,
        today: Int,
        total: Int,
        lastDate: String,
    ): String = """
        You are a motivational coach helping someone quit smoking. Respond with empathy, motivation, and a bit of humor.
        You are talking to $name.

        User data:
        - Smoked $today cigarettes today
        - Has $total cigarettes recorded
        - The last one was at $lastDate

        What would you say to motivate them?
    """.trimIndent()
}