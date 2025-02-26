package com.feragusper.smokeanalytics.libraries.wear.data

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module to provide the WearSyncManager as a singleton instance.
 *
 * This module is used to inject the WearSyncManager instance into other parts of the app
 * wherever it's needed.
 */
@Module
@InstallIn(SingletonComponent::class)  // Ensures that this module is installed in the SingletonComponent
object WearSyncModule {

    /**
     * Provides a singleton instance of WearSyncManager.
     *
     * @param context The application context, injected by Hilt using the @ApplicationContext qualifier.
     * @return A singleton instance of WearSyncManager.
     */
    @Provides
    @Singleton  // This annotation ensures that only one instance of WearSyncManager is used throughout the app
    fun provideWearSyncManager(@ApplicationContext context: Context): WearSyncManager {
        return WearSyncManager(context)  // Return an instance of WearSyncManager
    }
}
