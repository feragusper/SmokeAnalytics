package com.feragusper.smokeanalytics.libraries.wear.data

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WearSyncModule {

    @Provides
    @Singleton
    fun provideWearSyncManager(@ApplicationContext context: Context): WearSyncManager {
        return WearSyncManager(context)
    }
}
