package com.feragusper.smokeanalytics.features.chatbot.data

import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotRepository
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.google.ai.client.generativeai.GenerativeModel
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatbotRepositoryImpl @Inject constructor(
    private val gemini: GenerativeModel
) : ChatbotRepository {

    override suspend fun sendMessage(message: String) = runCatching {
        gemini.generateContent(message).text
    }.getOrElse {
        it.printStackTrace()
        "Ups, el coach tuvo un mal día y no pudo responder."
    } ?: "Sin respuesta del modelo."

    override suspend fun sendInitialMessageWithContext(
        name: String,
        recentSmokes: List<Smoke>
    ): String {
        val todayCount = recentSmokes.count { it.date.toLocalDate() == LocalDate.now() }
        val total = recentSmokes.size
        val lastDate = recentSmokes.firstOrNull()?.date?.toString() ?: "No registrado"

        val prompt = buildPrompt(name, todayCount, total, lastDate)

        return runCatching {
            gemini.generateContent(prompt).text
        }.getOrElse {
            it.printStackTrace()
            "No se pudo generar un mensaje motivacional. Probá más tarde."
        } ?: "Sin respuesta del modelo."
    }

    private fun buildPrompt(name: String, today: Int, total: Int, lastDate: String): String = """
        Sos un coach motivacional para dejar de fumar. Respondé con empatía, motivación y un poco de humor.
        Estás hablando con $name
        Datos del usuario:
        - Fumó $today cigarrillos hoy
        - Tiene $total cigarrillos registrados
        - El último fue el $lastDate

        ¿Qué le dirías para motivarlo?
    """.trimIndent()
}

