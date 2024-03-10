package com.feragusper.smokeanalytics.libraries.smokes.data.di

import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl
import com.feragusper.smokeanalytics.libraries.smokes.domain.SmokeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class SmokeRepositoryModule {
    @Binds
    abstract fun provideSmokeRepository(smokeRepository: SmokeRepositoryImpl): SmokeRepository
}
