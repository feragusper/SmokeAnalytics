package com.feragusper.smokeanalytics.features.chatbot.data

import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotRepository
import com.google.ai.client.generativeai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ChatbotRepositoryModule {

    @Provides
    @Singleton
    fun provideChatbotRepository(model: GenerativeModel): ChatbotRepository =
        ChatbotRepositoryImpl(model)

    @Provides
    @Singleton
    fun provideGemini(): GenerativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GOOGLE_AI_CLIENT_GENERATIVEAI_API_KEY
    )
}
