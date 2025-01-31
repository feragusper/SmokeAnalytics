package com.feragusper.smokeanalytics

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * The application class for SmokeAnalytics. Initializes global libraries and settings, such as Timber for logging.
 */
@HiltAndroidApp
class SmokeAnalyticsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
