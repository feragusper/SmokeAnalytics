package com.feragusper.smokeanalytics.libraries.smokes.data.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * A Dagger module that provides a singleton instance of [FirebaseFirestore], configured with custom settings,
 * particularly focused on enabling persistent caching and automatic index creation to enhance offline capabilities
 * and performance.
 */
@InstallIn(SingletonComponent::class)
@Module
internal object FirestoreModule {

    /**
     * Provides a [FirebaseFirestore] instance with customized settings, including enabled persistent cache
     * and automatic index creation for improved offline support and query performance.
     *
     * @return The configured [FirebaseFirestore] instance.
     */
    @Singleton
    @Provides
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        firestoreSettings = firestoreSettings {
            // Configuration to enable local persistent cache.
            setLocalCacheSettings(persistentCacheSettings {})
        }

        // Enable automatic index creation to optimize query performance.
        persistentCacheIndexManager?.enableIndexAutoCreation()
    }
}
