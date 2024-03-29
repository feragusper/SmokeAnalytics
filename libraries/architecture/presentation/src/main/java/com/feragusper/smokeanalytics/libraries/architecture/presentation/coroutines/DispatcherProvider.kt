package com.feragusper.smokeanalytics.libraries.architecture.presentation.coroutines

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Provides coroutine dispatchers to be injected for various asynchronous operations,
 * improving testability by allowing for dispatcher swapping in tests.
 */
interface DispatcherProvider {

    /**
     * Returns a [CoroutineDispatcher] for CPU-intensive background tasks.
     */
    fun default(): CoroutineDispatcher

    /**
     * Returns a [CoroutineDispatcher] for IO-intensive tasks like network calls or disk operations.
     */
    fun io(): CoroutineDispatcher

    /**
     * Returns a [CoroutineDispatcher] for tasks that interact with the UI, confining them to the main thread.
     */
    fun main(): CoroutineDispatcher
}