package com.feragusper.smokeanalytics.libraries.authentication.data

import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the binding for [AuthenticationRepository] to its implementation [AuthenticationRepositoryImpl],
 * allowing Dagger-Hilt to inject the concrete implementation wherever an [AuthenticationRepository] is required.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthenticationRepositoryModule {

    /**
     * Binds the implementation [AuthenticationRepositoryImpl] to the [AuthenticationRepository] interface.
     *
     * @param authenticationRepository The implementation to be used.
     * @return The [AuthenticationRepository] instance.
     */
    @Binds
    @Singleton
    abstract fun provideAuthenticationRepository(
        authenticationRepository: AuthenticationRepositoryImpl
    ): AuthenticationRepository
}
