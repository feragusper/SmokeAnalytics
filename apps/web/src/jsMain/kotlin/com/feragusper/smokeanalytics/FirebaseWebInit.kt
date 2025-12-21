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
        // Avoid double init in HMR / recomposition scenarios
        runCatching {
//            Firebase.initialize(
//                options = FirebaseOptions(
//                    apiKey = "AIzaSyCQsNHxeSiaXTr5KugYx4AmMxpflL_O9lI",
//                    authDomain = "smoke-analytics-staging.firebaseapp.com",
//                    projectId = "smoke-analytics-staging",
//                    storageBucket = "smoke-analytics-staging.firebasestorage.app", // optional if you use storage
//                    applicationId = "1:1016019974225:web:ed48cf5c4e50e5357ee070",
//                    //messagingSenderId: "1016019974225",
//                    //measurementId: "G-7MPPG1QTDD"
//                )
//            )

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

//// Import the functions you need from the SDKs you need
//import { initializeApp } from "firebase/app";
//import { getAnalytics } from "firebase/analytics";
//// TODO: Add SDKs for Firebase products that you want to use
//// https://firebase.google.com/docs/web/setup#available-libraries
//
//// Your web app's Firebase configuration
//// For Firebase JS SDK v7.20.0 and later, measurementId is optional
//const firebaseConfig = {
//    apiKey: "AIzaSyC4P6TscDf8CgRFvup2uouvixEVRklnYkc",
//    authDomain: "smoke-analytics.firebaseapp.com",
//    projectId: "smoke-analytics",
//    storageBucket: "smoke-analytics.firebasestorage.app",
//    messagingSenderId: "235081091876",
//    appId: "1:235081091876:web:1f590358b355fa999141b1",
//    measurementId: "G-QKWQM4SMN8"
//};
//
//// Initialize Firebase
//const app = initializeApp(firebaseConfig);
//const analytics = getAnalytics(app);