package com.feragusper.smokeanalytics.features.devtools.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session

/**
 * Represents the possible outcomes of processing [DevToolsIntent].
 *
 * This sealed class defines all the different states the DevTools module can be in,
 * based on the result of processing a user intent.
 */
sealed class DevToolsResult : MVIResult {

    /**
     * Indicates that the operation is in progress.
     * This is typically used to show a loading indicator.
     */
    object Loading : DevToolsResult()

    /**
     * Represents a state where the user is logged in.
     *
     * @property user Optional user session information, containing user details if available.
     */
    data class UserLoggedIn(val user: Session.User?) : DevToolsResult()

    /**
     * Indicates that the user is logged out or not currently authenticated.
     */
    object UserLoggedOut : DevToolsResult()
}
