package com.feragusper.smokeanalytics.libraries.architecture.common.coroutines.di

import com.feragusper.smokeanalytics.libraries.architecture.common.coroutines.DispatcherProvider
import com.feragusper.smokeanalytics.libraries.architecture.common.coroutines.DispatcherProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger module that provides [DispatcherProvider] implementations, ensuring that coroutine dispatchers
 * are correctly provided to consumers within the ViewModelComponent scope.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object ThreadingModule {

    /**
     * Provides an implementation of [DispatcherProvider] that uses the standard Kotlin coroutine dispatchers.
     */
    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DispatcherProviderImpl()
}
