package com.feragusper.smokeanalytics.libraries.architecture.presentation.process

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult
import kotlinx.coroutines.flow.Flow

/**
 * Processes intents into results, maintaining a single source of truth for intent handling.
 *
 * @param I The type of [MVIIntent] that this process holder can handle.
 * @param R The type of [MVIResult] that this process holder emits.
 */
fun interface MVIProcessHolder<I : MVIIntent, R : MVIResult> {

    /**
     * Transforms an [MVIIntent] into a stream of [MVIResult]s.
     *
     * @param intent The intent to process.
     * @return A [Flow] emitting the resulting [MVIResult]s.
     */
    fun processIntent(intent: I): Flow<R>
}
