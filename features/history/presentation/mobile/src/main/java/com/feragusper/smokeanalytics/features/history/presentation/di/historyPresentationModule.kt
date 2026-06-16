package com.feragusper.smokeanalytics.features.history.presentation.di

import com.feragusper.smokeanalytics.features.history.presentation.HistoryViewModel
import com.feragusper.smokeanalytics.features.history.presentation.process.HistoryProcessHolder
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val historyPresentationModule = module {
    factoryOf(::HistoryProcessHolder)
    viewModelOf(::HistoryViewModel)
}
