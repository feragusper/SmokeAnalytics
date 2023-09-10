package com.feragusper.smokeanalytics.features.profile.presentation

import com.feragusper.smokeanalytics.features.profile.navigation.ProfileNavigator
import com.feragusper.smokeanalytics.features.profile.presentation.mvi.ProfileIntent
import com.feragusper.smokeanalytics.features.profile.presentation.mvi.ProfileResult
import com.feragusper.smokeanalytics.features.profile.presentation.mvi.ProfileViewState
import com.feragusper.smokeanalytics.features.profile.presentation.process.ProfileProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val processHolder: ProfileProcessHolder,
) : MVIViewModel<ProfileIntent, ProfileViewState, ProfileResult, ProfileNavigator>(initialState = ProfileViewState()) {

    override lateinit var navigator: ProfileNavigator

    init {
        intents().trySend(ProfileIntent.FetchUser)
    }

    override suspend fun transformer(intent: ProfileIntent) = processHolder.processIntent(intent)

    override suspend fun reducer(
        previous: ProfileViewState,
        result: ProfileResult,
    ): ProfileViewState = when (result) {
        is ProfileResult.UserLoggedIn -> previous.copy(
            displayLoading = false,
            currentUserName = result.displayName,
        )

        ProfileResult.UserLoggedOut -> previous.copy(
            displayLoading = false,
            currentUserName = null,
        )

        ProfileResult.Loading -> previous.copy(displayLoading = true)
    }
}
