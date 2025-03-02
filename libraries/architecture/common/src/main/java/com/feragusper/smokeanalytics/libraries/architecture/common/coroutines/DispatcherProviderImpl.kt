package com.feragusper.smokeanalytics.libraries.architecture.common.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Provides coroutine dispatchers to be injected for various asynchronous operations,
 * improving testability by allowing for dispatcher swapping in tests.
 */
class DispatcherProviderImpl : DispatcherProvider {

    override fun default(): CoroutineDispatcher = Dispatchers.Default

    override fun io(): CoroutineDispatcher = Dispatchers.IO

    override fun main(): CoroutineDispatcher = Dispatchers.Main
}
