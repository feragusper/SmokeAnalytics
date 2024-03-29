package com.feragusper.smokeanalytics.libraries.authentication.data

import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

/**
 * Provides the binding for [AuthenticationRepository] to its implementation [AuthenticationRepositoryImpl],
 * allowing Dagger-Hilt to inject the concrete implementation where an AuthenticationRepository is required.
 */
@Module
@InstallIn(ViewModelComponent::class)
abstract class AuthenticationRepositoryModule {
    @Binds
    abstract fun provideAuthenticationRepository(authenticationRepository: AuthenticationRepositoryImpl): AuthenticationRepository
}
