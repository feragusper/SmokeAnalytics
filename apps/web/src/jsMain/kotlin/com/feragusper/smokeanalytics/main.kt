package com.feragusper.smokeanalytics

import com.feragusper.smokeanalytics.features.goals.domain.di.goalsDomainModule
import com.feragusper.smokeanalytics.features.home.domain.di.homeDomainModule
import com.feragusper.smokeanalytics.libraries.authentication.domain.di.authenticationDomainModule
import com.feragusper.smokeanalytics.libraries.cravings.domain.di.cravingsDomainModule
import com.feragusper.smokeanalytics.libraries.design.SmokeWebTheme
import com.feragusper.smokeanalytics.libraries.preferences.domain.di.preferencesDomainModule
import com.feragusper.smokeanalytics.libraries.smokes.domain.di.smokesDomainModule
import org.jetbrains.compose.web.renderComposable
import org.koin.core.context.startKoin

fun main() {
    FirebaseWebInit.init()
    startKoin {
        modules(
            // shared domain use cases
            smokesDomainModule,
            cravingsDomainModule,
            preferencesDomainModule,
            authenticationDomainModule,
            homeDomainModule,
            goalsDomainModule,
            // web repositories, services, process holders and stores
            webModule,
        )
    }

    renderComposable(rootElementId = "root") {
        SmokeWebTheme {
            AppRoot()
        }
    }
}
