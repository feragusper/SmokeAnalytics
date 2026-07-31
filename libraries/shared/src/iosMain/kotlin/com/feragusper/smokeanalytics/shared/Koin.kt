package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.features.goals.domain.di.goalsDomainModule
import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.domain.di.homeDomainModule
import com.feragusper.smokeanalytics.libraries.authentication.domain.di.authenticationDomainModule
import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.feragusper.smokeanalytics.libraries.cravings.domain.di.cravingsDomainModule
import com.feragusper.smokeanalytics.libraries.preferences.domain.di.preferencesDomainModule
import com.feragusper.smokeanalytics.libraries.smokes.domain.di.smokesDomainModule
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

/** All Koin modules the iOS app runs with: shared domain use cases + iOS repositories/services. */
private val iosKoinModules = listOf(
    smokesDomainModule,
    cravingsDomainModule,
    preferencesDomainModule,
    authenticationDomainModule,
    homeDomainModule,
    goalsDomainModule,
    iosDataModule,
)

/**
 * Starts Koin for the iOS app. Call once from Swift on launch, AFTER `FirebaseApp.configure()`
 * so the GitLive repositories find a configured Firebase instance.
 */
fun doInitKoin() {
    startKoin {
        modules(iosKoinModules)
    }
}

/**
 * Swift-facing accessor for the shared use cases. Kotlin `by inject()` keeps Swift free of the
 * Koin API: `IosUseCases().fetchSmokesUseCase` resolves from the started Koin graph.
 */
class IosUseCases : KoinComponent {
    val fetchSmokes: FetchSmokesUseCase by inject()
    val fetchSmokeCountList: FetchSmokeCountListUseCase by inject()
    val addSmoke: AddSmokeUseCase by inject()
    val deleteSmoke: DeleteSmokeUseCase by inject()
    val fetchSmokeStats: FetchSmokeStatsUseCase by inject()
    val authenticationRepository: AuthenticationRepository by inject()
}
