package com.feragusper.smokeanalytics.libraries.smokes.data.di

import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module providing the smokes data layer, including the app-wide
 * [FirebaseFirestore] instance (configured with a persistent cache) reused by the
 * other data modules.
 */
val smokesDataModule = module {
    single<FirebaseFirestore> {
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = firestoreSettings {
                setLocalCacheSettings(persistentCacheSettings {})
            }
            persistentCacheIndexManager?.enableIndexAutoCreation()
        }
    }
    single<SmokeRepository> { SmokeRepositoryImpl(get(), get(), androidContext()) }
}
