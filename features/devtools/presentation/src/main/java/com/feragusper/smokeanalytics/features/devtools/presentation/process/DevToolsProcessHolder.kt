package com.feragusper.smokeanalytics.features.devtools.presentation.process

import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsIntent
import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Handles the business logic of processing intents for the DevTools feature.
 *
 * This class is responsible for transforming [DevToolsIntent] into [DevToolsResult]s,
 * encapsulating the application's business logic for developer tools functionality.
 *
 * @property fetchSessionUseCase Use case for fetching the current session information.
 */
class DevToolsProcessHolder @Inject constructor(
    private val fetchSessionUseCase: FetchSessionUseCase,
) : MVIProcessHolder<DevToolsIntent, DevToolsResult> {

    /**
     * Processes an [DevToolsIntent] and transforms it into a stream of [DevToolsResult]s.
     *
     * @param intent The user intent to be processed.
     * @return A [Flow] emitting the corresponding [DevToolsResult]s.
     */
    override fun processIntent(intent: DevToolsIntent): Flow<DevToolsResult> = when (intent) {
        DevToolsIntent.FetchUser -> processFetchUser()
    }

    /**
     * Handles the [DevToolsIntent.FetchUser] intent by fetching the user session state
     * and emitting the corresponding [DevToolsResult].
     *
     * @return A [Flow] emitting the result of the user session fetch operation.
     */
    private fun processFetchUser(): Flow<DevToolsResult> = flow {
        emit(DevToolsResult.Loading)
        when (val session = fetchSessionUseCase()) {
            is Session.Anonymous -> emit(DevToolsResult.UserLoggedOut)
            is Session.LoggedIn -> emit(DevToolsResult.UserLoggedIn(session.user))
        }
    }
}
