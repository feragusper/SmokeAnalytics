package com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import timber.log.Timber

/**
 * Catches exceptions that occur within the flow and logs them using Timber, allowing
 * for custom handling via [action].
 *
 * @param action A suspend function that allows for handling of the caught [Throwable].
 * @return The original flow with exception handling applied.
 */
fun <T> Flow<T>.catchAndLog(
    action: suspend FlowCollector<T>.(cause: Throwable) -> Unit,
) = this.catch { throwable ->
    // Log the error using Timber.e with the throwable and a message.
    Timber.e(throwable, "Error caught")
    action(throwable)
}