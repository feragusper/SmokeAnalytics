package com.feragusper.smokeanalytics.libraries.smokes.data.di

import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl
import com.feragusper.smokeanalytics.libraries.smokes.domain.SmokeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

/**
 * Defines a module for binding the [SmokeRepository] interface to its implementation, [SmokeRepositoryImpl],
 * facilitating dependency injection of the repository across the application, especially within ViewModel components.
 */
@Module
@InstallIn(ViewModelComponent::class)
abstract class SmokeRepositoryModule {

    /**
     * Binds the [SmokeRepositoryImpl] class to the [SmokeRepository] interface, allowing Hilt to inject
     * the concrete implementation where a SmokeRepository is required.
     *
     * @param smokeRepository The implementation of [SmokeRepository] to be injected.
     */
    @Binds
    abstract fun provideSmokeRepository(smokeRepository: SmokeRepositoryImpl): SmokeRepository
}
