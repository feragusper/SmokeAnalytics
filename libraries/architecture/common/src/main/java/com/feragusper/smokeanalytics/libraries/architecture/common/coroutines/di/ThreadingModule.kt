package com.feragusper.smokeanalytics.libraries.architecture.common.coroutines.di

import com.feragusper.smokeanalytics.libraries.architecture.common.coroutines.DispatcherProvider
import com.feragusper.smokeanalytics.libraries.architecture.common.coroutines.DispatcherProviderImpl
import org.koin.dsl.module

/**
 * Koin module providing the [DispatcherProvider] used to swap coroutine dispatchers in tests.
 */
val threadingModule = module {
    single<DispatcherProvider> { DispatcherProviderImpl() }
}
