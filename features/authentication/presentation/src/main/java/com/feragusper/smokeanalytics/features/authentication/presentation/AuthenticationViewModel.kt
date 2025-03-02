package com.feragusper.smokeanalytics.features.authentication.presentation

import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationIntent
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationResult
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationResult.Error
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationResult.Loading
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.compose.AuthenticationViewState
import com.feragusper.smokeanalytics.features.authentication.presentation.navigation.AuthenticationNavigator
import com.feragusper.smokeanalytics.features.authentication.presentation.process.AuthenticationProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the authentication feature, managing UI state based on user intents and processing results.
 *
 * It extends [MVIViewModel] to implement the Model-View-Intent (MVI) architecture pattern.
 * This ViewModel handles user authentication logic and updates the UI state accordingly.
 *
 * @property processHolder Encapsulates business logic to process [AuthenticationIntent] into [AuthenticationResult].
 */
@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val processHolder: AuthenticationProcessHolder,
) : MVIViewModel<AuthenticationIntent, AuthenticationViewState, AuthenticationResult, AuthenticationNavigator>(
    initialState = AuthenticationViewState()
) {

    /**
     * Navigator instance for handling navigation actions.
     */
    override lateinit var navigator: AuthenticationNavigator

    /**
     * Transforms [AuthenticationIntent] into a stream of [AuthenticationResult]s.
     *
     * @param intent The user intent to be processed.
     * @return A Flow of [AuthenticationResult] representing the result of processing the intent.
     */
    override fun transformer(intent: AuthenticationIntent) =
        processHolder.processIntent(intent)

    /**
     * Reduces the previous [AuthenticationViewState] and a new [AuthenticationResult] to a new state.
     *
     * This function is responsible for creating the new state based on the current state and the result.
     *
     * @param previous The previous state of the UI.
     * @param result The result of processing the intent.
     * @return The new state of the UI.
     */
    override suspend fun reducer(
        previous: AuthenticationViewState,
        result: AuthenticationResult
    ): AuthenticationViewState =
        when (result) {
            Loading -> previous.copy(
                displayLoading = true,
                error = null,
            )

            is Error -> previous.copy(
                displayLoading = false,
                error = result,
            )

            AuthenticationResult.NavigateUp,
            AuthenticationResult.UserLoggedIn -> {
                navigator.navigateUp()
                previous
            }
        }
}
