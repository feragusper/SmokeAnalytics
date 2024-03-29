package com.feragusper.smokeanalytics.libraries.architecture.presentation.process

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult
import kotlinx.coroutines.flow.Flow

/**
 * Processes intents into results, maintaining a single source of truth for intent handling.
 */
interface MVIProcessHolder<I : MVIIntent, R : MVIResult> {

    /**
     * Transforms an [MVIIntent] into a stream of [MVIResult]s.
     */
    fun processIntent(intent: I): Flow<R>
}