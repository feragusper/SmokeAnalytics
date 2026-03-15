package com.feragusper.smokeanalytics.libraries.preferences.data.di

import com.feragusper.smokeanalytics.libraries.preferences.data.UserPreferencesRepositoryImpl
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserPreferencesModule {
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl,
    ): UserPreferencesRepository
}
