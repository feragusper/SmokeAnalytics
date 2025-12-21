package com.feragusper.smokeanalytics.features.home.presentation.web

import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase

class HomeWebDependencies(
    val homeProcessHolder: HomeProcessHolder,
)

fun createHomeWebDependencies(
    fetchSessionUseCase: FetchSessionUseCase,
    fetchSmokeCountListUseCase: FetchSmokeCountListUseCase,
    addSmokeUseCase: AddSmokeUseCase,
    editSmokeUseCase: EditSmokeUseCase,
    deleteSmokeUseCase: DeleteSmokeUseCase,
): HomeWebDependencies {
    return HomeWebDependencies(
        homeProcessHolder = HomeProcessHolder(
            addSmokeUseCase = addSmokeUseCase,
            editSmokeUseCase = editSmokeUseCase,
            deleteSmokeUseCase = deleteSmokeUseCase,
            fetchSmokeCountListUseCase = fetchSmokeCountListUseCase,
            fetchSessionUseCase = fetchSessionUseCase,
        )
    )
}