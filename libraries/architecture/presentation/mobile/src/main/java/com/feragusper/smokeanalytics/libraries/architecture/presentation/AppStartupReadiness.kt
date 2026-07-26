package com.feragusper.smokeanalytics.libraries.architecture.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Process-wide signal that the app's first meaningful content (the Home screen's initial load)
 * is ready to be shown.
 *
 * The cold-start splash holds until [isReady] flips to true, so the home content is fully
 * populated before it is revealed — no loading skeleton flashes behind the splash.
 */
object AppStartupReadiness {

    private val _isReady = MutableStateFlow(false)

    /** Emits false while the initial load is in flight, then true once content is ready. */
    val isReady: StateFlow<Boolean> = _isReady

    /** Called by the first screen once its initial data (or a terminal error) has resolved. */
    fun markReady() {
        _isReady.value = true
    }

    /** Re-arms the signal at the start of a cold-start splash so a relaunch waits again. */
    fun reset() {
        _isReady.value = false
    }
}
