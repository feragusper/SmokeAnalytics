package com.feragusper.smokeanalytics.features.goals.presentation.navigation

import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator

class GoalsNavigator(
    private val navigateBack: () -> Unit = {},
) : MVINavigator {

    fun navigateBack() {
        navigateBack.invoke()
    }
}
