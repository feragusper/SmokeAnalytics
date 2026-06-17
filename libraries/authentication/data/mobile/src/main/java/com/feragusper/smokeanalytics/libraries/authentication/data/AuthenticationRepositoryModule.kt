package com.feragusper.smokeanalytics.libraries.authentication.data

import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import org.koin.dsl.module

/**
 * Koin module providing the authentication data layer, including the app-wide
 * [FirebaseAuth] instance reused by other data modules.
 */
val authenticationDataModule = module {
    single<FirebaseAuth> { Firebase.auth }
    single<AuthenticationRepository> { AuthenticationRepositoryImpl(get()) }
}
