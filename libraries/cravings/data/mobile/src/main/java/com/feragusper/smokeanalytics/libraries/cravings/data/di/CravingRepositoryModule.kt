package com.feragusper.smokeanalytics.libraries.cravings.data.di

import com.feragusper.smokeanalytics.libraries.cravings.data.CravingRepositoryImpl
import com.feragusper.smokeanalytics.libraries.cravings.domain.repository.CravingRepository
import org.koin.dsl.module

/**
 * Koin module providing the cravings data layer. Reuses the FirebaseFirestore and
 * FirebaseAuth singletons provided by the smokes and authentication data modules.
 */
val cravingsDataModule = module {
    single<CravingRepository> { CravingRepositoryImpl(get(), get()) }
}
