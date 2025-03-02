package com.feragusper.smokeanalytics.libraries.wear.data.di

import android.content.Context
import com.feragusper.smokeanalytics.libraries.architecture.common.coroutines.DispatcherProvider
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.feragusper.smokeanalytics.libraries.wear.data.WearSyncManagerImpl
import com.feragusper.smokeanalytics.libraries.wear.domain.WearSyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
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
     * @param smokeRepository The repository for managing smoke data.
     *
     * @return A singleton instance of WearSyncManager.
     */
    @Provides
    @Singleton
    fun provideWearSyncManager(
        @ApplicationContext context: Context,
        smokeRepository: SmokeRepository,
        dispatcherProvider: DispatcherProvider
    ): WearSyncManager.Mobile {
        return WearSyncManagerImpl(context, dispatcherProvider).Mobile(
            coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default()),
            smokeRepository = smokeRepository
        )
    }
}
