package com.feragusper.smokeanalytics

import com.feragusper.smokeanalytics.apps.web.BuildKonfig
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

/**
 * Initializes Firebase for the web application.
 */
object FirebaseWebInit {

    /**
     * Initializes Firebase with the provided options.
     */
    fun init() {
        runCatching {
            Firebase.initialize(
                options = FirebaseOptions(
                    apiKey = BuildKonfig.FIREBASE_API_KEY,
                    authDomain = BuildKonfig.FIREBASE_AUTH_DOMAIN,
                    projectId = BuildKonfig.FIREBASE_PROJECT_ID,
                    storageBucket = BuildKonfig.FIREBASE_STORAGE_BUCKET,
                    applicationId = BuildKonfig.FIREBASE_APP_ID,
                )
            )
        }
    }
}