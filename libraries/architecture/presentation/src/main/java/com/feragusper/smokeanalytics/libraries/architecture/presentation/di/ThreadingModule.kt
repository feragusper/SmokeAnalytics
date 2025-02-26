package com.feragusper.smokeanalytics.libraries.architecture.presentation.di

import com.feragusper.smokeanalytics.libraries.architecture.presentation.coroutines.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Dagger module that provides [DispatcherProvider] implementations, ensuring that coroutine dispatchers
 * are correctly provided to consumers within the ViewModelComponent scope.
 */
@Module
@InstallIn(ViewModelComponent::class)
internal object ThreadingModule {

    /**
     * Provides an implementation of [DispatcherProvider] that uses the standard Kotlin coroutine dispatchers.
     */
    @Provides
    fun provideDispatcherProvider(): DispatcherProvider = object : DispatcherProvider {
        override fun default(): CoroutineDispatcher = Dispatchers.Default

        override fun io(): CoroutineDispatcher = Dispatchers.IO

        override fun main(): CoroutineDispatcher = Dispatchers.Main
    }
}
