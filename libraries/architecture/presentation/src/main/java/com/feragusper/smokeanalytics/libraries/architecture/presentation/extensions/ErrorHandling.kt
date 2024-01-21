package com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import timber.log.Timber

fun <T> Flow<T>.catchAndLog(
    action: suspend FlowCollector<T>.(cause: Throwable) -> Unit,
) = this.catch {
    Timber.e("ErrorHandling", "Error caught", it)
    action(it)
}

