package com.feragusper.smokeanalytics.apps.web

import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotRepository
import com.feragusper.smokeanalytics.features.chatbot.domain.CoachContext
import com.feragusper.smokeanalytics.features.chatbot.domain.CoachReply
import com.feragusper.smokeanalytics.features.chatbot.domain.CoachReplySource
import com.feragusper.smokeanalytics.features.chatbot.domain.fallbackCoachReply
import com.feragusper.smokeanalytics.features.chatbot.domain.fallbackInitialCoachMessage
import dev.gitlive.firebase.auth.externals.getAuth
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.RequestInit

class CoachRelayRepository : ChatbotRepository {

    override suspend fun sendMessage(message: String, context: CoachContext): CoachReply =
        requestCoachReply(
            kind = "message",
            context = context,
            message = message,
            fallbackText = fallbackCoachReply(message, context),
        )

    override suspend fun sendInitialMessage(context: CoachContext): CoachReply =
        requestCoachReply(
            kind = "initial",
            context = context,
            message = null,
            fallbackText = fallbackInitialCoachMessage(context),
        )

    private suspend fun requestCoachReply(
        kind: String,
        context: CoachContext,
        message: String?,
        fallbackText: String,
    ): CoachReply {
        val idToken = currentUserIdToken() ?: return CoachReply(
            text = fallbackText,
            source = CoachReplySource.Fallback,
        )

        return runCatching {
            val payload = js("{}")
            payload.kind = kind
            payload.context = context.toRelayContext()
            if (message != null) {
                payload.message = message.take(MAX_MESSAGE_LENGTH)
            }

            val requestInit = js("{}").unsafeCast<RequestInit>()
            requestInit.method = "POST"
            requestInit.headers = js(
                """({
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + idToken
                })"""
            )
            requestInit.body = JSON.stringify(payload)

            val response = window.fetch("/api/coach", requestInit).await()
            if (!response.ok) error("Relay request failed with ${'$'}{response.status}")

            val json = response.json().await().unsafeCast<dynamic>()
            val text = (json.text as? String)?.trim().orEmpty()
            if (text.isBlank()) error("Relay response was empty")

            CoachReply(
                text = text,
                source = when ((json.source as? String)?.lowercase()) {
                    "live" -> CoachReplySource.Live
                    else -> CoachReplySource.Fallback
                },
            )
        }.getOrElse {
            CoachReply(
                text = fallbackText,
                source = CoachReplySource.Fallback,
            )
        }
    }

    private suspend fun currentUserIdToken(): String? {
        val currentUser = getAuth().currentUser ?: return null
        return currentUser.asDynamic().getIdToken().await() as? String
    }

    private fun CoachContext.toRelayContext(): dynamic {
        val payload = js("{}")
        payload.name = name
        payload.todayCount = todayCount
        payload.weekCount = weekCount
        payload.monthCount = monthCount
        payload.totalCount = totalCount
        payload.hoursSinceLastSmoke = hoursSinceLastSmoke
        payload.minutesSinceLastSmoke = minutesSinceLastSmoke
        payload.currentStreakHours = currentStreakHours
        payload.longestStreakHours = longestStreakHours
        payload.averageGapMinutes = averageGapMinutes
        return payload
    }

    private companion object {
        const val MAX_MESSAGE_LENGTH = 240
    }
}
