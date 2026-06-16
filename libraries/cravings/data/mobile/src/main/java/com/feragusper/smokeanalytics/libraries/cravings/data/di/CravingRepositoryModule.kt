package com.feragusper.smokeanalytics.libraries.cravings.data.di

import com.feragusper.smokeanalytics.libraries.cravings.data.CravingRepositoryImpl
import com.feragusper.smokeanalytics.libraries.cravings.domain.repository.CravingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds [CravingRepository] to its Firestore-backed implementation.
 *
 * The [com.google.firebase.firestore.FirebaseFirestore] and
 * [com.google.firebase.auth.FirebaseAuth] dependencies are provided by the
 * smokes and authentication data modules respectively, and are reused here.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CravingRepositoryModule {

    @Binds
    @Singleton
    abstract fun provideCravingRepository(impl: CravingRepositoryImpl): CravingRepository
}
