package com.feragusper.smokeanalytics

import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotRepository
import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotUseCase
import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCasesModule {

    @Provides
    fun provideFetchSessionUseCase(
        repo: AuthenticationRepository
    ) = FetchSessionUseCase(repo)

    @Provides
    fun provideSignOutUseCase(
        repo: AuthenticationRepository
    ) = SignOutUseCase(repo)

    @Provides
    fun provideAddSmokeUseCase(
        repo: SmokeRepository
    ) = AddSmokeUseCase(repo)

    @Provides
    fun provideEditSmokeUseCase(
        repo: SmokeRepository
    ) = EditSmokeUseCase(repo)

    @Provides
    fun provideDeleteSmokeUseCase(
        repo: SmokeRepository
    ) = DeleteSmokeUseCase(repo)

    @Provides
    fun provideFetchSmokesUseCase(
        repo: SmokeRepository
    ) = FetchSmokesUseCase(repo)

    @Provides
    fun provideFetchSmokeStatsUseCase(
        repo: SmokeRepository
    ) = FetchSmokeStatsUseCase(repo)

    @Provides
    fun provideFetchSmokeCountListUseCase(
        repo: SmokeRepository
    ) = FetchSmokeCountListUseCase(repo)

    @Provides
    fun provideChatbotUseCase(
        smokeRepository: SmokeRepository,
        authenticationRepository: AuthenticationRepository,
        chatbotRepository: ChatbotRepository
    ) = ChatbotUseCase(
        smokeRepository = smokeRepository,
        authRepository = authenticationRepository,
        chatbotRepository = chatbotRepository
    )
}