package com.feragusper.smokeanalytics.apps.web

import androidx.compose.runtime.Composable
import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotUseCase
import com.feragusper.smokeanalytics.libraries.design.EmptyStateCard
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun CoachWebScreen(
    chatbotUseCase: ChatbotUseCase,
) {
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "Coach",
            eyebrow = "Support",
            badgeText = "Coming soon",
        )

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Why it's paused on web") }
            Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                Text("The mobile coach can use a real model with smoking context. Web is still on a local fallback, so it is hidden until the live model integration is ready.")
            }
        }

        EmptyStateCard(
            title = "Coach is coming soon to web",
            message = "The next web version should ship with a real free-tier model, your recent smoking context, and clear coaching prompts instead of placeholder replies.",
        )
    }
}
