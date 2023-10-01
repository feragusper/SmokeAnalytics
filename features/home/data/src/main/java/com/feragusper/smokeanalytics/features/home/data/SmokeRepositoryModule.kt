package com.feragusper.smokeanalytics.features.home.data

import com.feragusper.smokeanalytics.features.home.domain.SmokeRepository
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
