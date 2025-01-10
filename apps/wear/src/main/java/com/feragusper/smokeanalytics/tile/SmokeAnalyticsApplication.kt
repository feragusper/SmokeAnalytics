package com.feragusper.smokeanalytics.tile

import android.app.Application
import com.feragusper.smokeanalytics.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * The application class for SmokeAnalytics. Initializes global libraries and settings, such as Timber for logging.
 */
@HiltAndroidApp
class SmokeAnalyticsApplication : Application() {

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    companion object {
        lateinit var instance: SmokeAnalyticsApplication
    }
}
