package com.feragusper.smokeanalytics.features.settings.presentation.di

import com.feragusper.smokeanalytics.features.settings.presentation.SettingsViewModel
import com.feragusper.smokeanalytics.features.settings.presentation.process.SettingsProcessHolder
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val settingsPresentationModule = module {
    factoryOf(::SettingsProcessHolder)
    viewModelOf(::SettingsViewModel)
}
