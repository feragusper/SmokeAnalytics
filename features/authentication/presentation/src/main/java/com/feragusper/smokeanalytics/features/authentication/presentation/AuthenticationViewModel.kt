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
 * ViewModel for the history feature, managing UI state based on user intents and processing results.
 *
 * @property processHolder Encapsulates business logic to process [AuthenticationIntent] into [AuthenticationResult].
 */
@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val processHolder: AuthenticationProcessHolder,
) : MVIViewModel<AuthenticationIntent, AuthenticationViewState, AuthenticationResult, AuthenticationNavigator>(
    initialState = AuthenticationViewState()
) {

    override lateinit var navigator: AuthenticationNavigator

    override suspend fun transformer(intent: AuthenticationIntent) =
        processHolder.processIntent(intent)

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

            AuthenticationResult.NavigateUp -> {
                navigator.navigateUp()
                previous
            }

            AuthenticationResult.UserLoggedIn -> {
                navigator.navigateUp()
                previous
            }
        }
}
