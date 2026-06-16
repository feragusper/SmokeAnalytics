package com.feragusper.smokeanalytics.libraries.preferences.data.di

import com.feragusper.smokeanalytics.libraries.preferences.data.UserPreferencesRepositoryImpl
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferencesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module providing the preferences data layer.
 *
 * FirebaseFirestore and FirebaseAuth are provided by the smokes and authentication
 * data modules respectively and resolved here at runtime.
 */
val preferencesDataModule = module {
    single<UserPreferencesRepository> {
        UserPreferencesRepositoryImpl(get(), get(), androidContext())
    }
}
