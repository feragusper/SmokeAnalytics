package com.feragusper.smokeanalytics.libraries.smokes.domain.di

import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Koin module exposing the smokes use cases (shared across mobile and web). */
val smokesDomainModule = module {
    factoryOf(::AddSmokeUseCase)
    factoryOf(::EditSmokeUseCase)
    factoryOf(::DeleteSmokeUseCase)
    factoryOf(::FetchSmokesUseCase)
    // Explicit factory (not factoryOf): the constructor DSL would try to resolve
    // the defaulted TimeZone parameter from the graph, which isn't provided.
    factory { FetchSmokeStatsUseCase(smokeRepository = get()) }
}
