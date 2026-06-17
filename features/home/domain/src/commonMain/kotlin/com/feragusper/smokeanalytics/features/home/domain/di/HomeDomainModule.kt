package com.feragusper.smokeanalytics.features.home.domain.di

import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Koin module exposing the home domain use cases (shared across mobile and web). */
val homeDomainModule = module {
    factoryOf(::FetchSmokeCountListUseCase)
}
