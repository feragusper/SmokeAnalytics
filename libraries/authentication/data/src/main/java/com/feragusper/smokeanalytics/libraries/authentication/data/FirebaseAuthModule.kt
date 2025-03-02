package com.feragusper.smokeanalytics.libraries.authentication.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides a singleton instance of [FirebaseAuth] to be used application-wide.
 * This is installed in the [SingletonComponent] to ensure it's available for injection throughout the app.
 */
@InstallIn(SingletonComponent::class)
@Module
internal object FirebaseAuthModule {

    /**
     * Provides the singleton instance of [FirebaseAuth] using Firebase's KTX extension.
     *
     * @return The [FirebaseAuth] instance.
     */
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth
}
