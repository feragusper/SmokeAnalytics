package com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi

import androidx.compose.runtime.Composable

/**
 * A data class implementing this interface represents a specific state of the UI.
 *
 * @param I The type of [MVIIntent] that this state responds to.
 */
fun interface MVIViewState<I : MVIIntent> {

    /**
     * Defines how the UI should be rendered based on the current state and allows for intentions to be emitted based on user actions.
     *
     * @param intent A function to emit user intentions as [MVIIntent]s.
     */
    @Composable
    fun Compose(intent: (I) -> Unit)
}
