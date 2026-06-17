package com.feragusper.smokeanalytics.libraries.wear.data.di

import com.feragusper.smokeanalytics.libraries.wear.data.WearSyncManagerImpl
import com.feragusper.smokeanalytics.libraries.wear.domain.WearSyncManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module providing the mobile-side Wear sync manager.
 */
val wearDataModule = module {
    single<WearSyncManager.Mobile> {
        WearSyncManagerImpl(androidContext(), get()).Mobile(get(), get(), get())
    }
}
