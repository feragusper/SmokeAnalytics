package com.feragusper.smokeanalytics.libraries.smokes.data.di

import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl
import com.feragusper.smokeanalytics.libraries.smokes.domain.SmokeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Defines a module for binding the [SmokeRepository] interface to its implementation, [SmokeRepositoryImpl],
 * facilitating dependency injection of the repository across the application, especially within ViewModel components.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SmokeRepositoryModule {


    @Binds
    @Singleton
    abstract fun provideSmokeRepository(smokeRepository: SmokeRepositoryImpl): SmokeRepository
}
