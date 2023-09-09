package com.feragusper.smokeanalytics.libraries.authentication.data

import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class AuthenticationRepositoryModule {
    @Binds
    abstract fun provideAuthenticationRepository(authenticationRepository: AuthenticationRepositoryImpl): AuthenticationRepository
}