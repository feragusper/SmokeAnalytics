package com.feragusper.smokeanalytics.libraries.cravings.domain.di

import com.feragusper.smokeanalytics.libraries.cravings.domain.CravingWaitCalculator
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.AddCravingUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.DeleteCravingUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.FetchActiveCravingUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.FetchCravingsUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.ResolveCravingUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Koin module exposing the cravings use cases (shared across mobile and web). */
val cravingsDomainModule = module {
    factoryOf(::AddCravingUseCase)
    factoryOf(::DeleteCravingUseCase)
    factoryOf(::FetchCravingsUseCase)
    factoryOf(::FetchActiveCravingUseCase)
    factoryOf(::ResolveCravingUseCase)
    factory { CravingWaitCalculator() }
}
