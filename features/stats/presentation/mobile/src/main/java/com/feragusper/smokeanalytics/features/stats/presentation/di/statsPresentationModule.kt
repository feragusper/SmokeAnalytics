package com.feragusper.smokeanalytics.features.stats.presentation.di

import com.feragusper.smokeanalytics.features.stats.presentation.StatsViewModel
import com.feragusper.smokeanalytics.features.stats.presentation.process.StatsProcessHolder
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val statsPresentationModule = module {
    factoryOf(::StatsProcessHolder)
    viewModelOf(::StatsViewModel)
}
