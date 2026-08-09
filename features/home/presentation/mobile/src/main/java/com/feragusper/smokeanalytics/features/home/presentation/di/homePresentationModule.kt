package com.feragusper.smokeanalytics.features.home.presentation.di

import com.feragusper.smokeanalytics.features.home.presentation.HomeViewModel
import com.feragusper.smokeanalytics.features.home.presentation.process.HomeProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.domain.ReadFreshnessGate
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val homePresentationModule = module {
    // Single so the cache/server freshness window is shared across the (per-request) process holders.
    single { ReadFreshnessGate() }
    factoryOf(::HomeProcessHolder)
    viewModelOf(::HomeViewModel)
}
