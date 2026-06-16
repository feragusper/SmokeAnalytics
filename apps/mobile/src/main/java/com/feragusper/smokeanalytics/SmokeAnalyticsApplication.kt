package com.feragusper.smokeanalytics

import android.app.Application
import com.feragusper.smokeanalytics.features.authentication.presentation.di.authenticationPresentationModule
import com.feragusper.smokeanalytics.features.devtools.presentation.di.devToolsPresentationModule
import com.feragusper.smokeanalytics.features.goals.domain.di.goalsDomainModule
import com.feragusper.smokeanalytics.features.goals.presentation.di.goalsPresentationModule
import com.feragusper.smokeanalytics.features.history.presentation.di.historyPresentationModule
import com.feragusper.smokeanalytics.features.home.domain.di.homeDomainModule
import com.feragusper.smokeanalytics.features.home.presentation.di.homePresentationModule
import com.feragusper.smokeanalytics.features.settings.presentation.di.settingsPresentationModule
import com.feragusper.smokeanalytics.features.stats.presentation.di.statsPresentationModule
import com.feragusper.smokeanalytics.libraries.architecture.common.coroutines.di.threadingModule
import com.feragusper.smokeanalytics.libraries.authentication.data.authenticationDataModule
import com.feragusper.smokeanalytics.libraries.authentication.domain.di.authenticationDomainModule
import com.feragusper.smokeanalytics.libraries.cravings.data.di.cravingsDataModule
import com.feragusper.smokeanalytics.libraries.cravings.domain.di.cravingsDomainModule
import com.feragusper.smokeanalytics.libraries.preferences.data.di.preferencesDataModule
import com.feragusper.smokeanalytics.libraries.preferences.domain.di.preferencesDomainModule
import com.feragusper.smokeanalytics.libraries.smokes.data.di.smokesDataModule
import com.feragusper.smokeanalytics.libraries.smokes.domain.di.smokesDomainModule
import com.feragusper.smokeanalytics.libraries.wear.data.di.wearDataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

/**
 * The application class for SmokeAnalytics.
 * Initializes Koin dependency injection and global libraries such as Timber.
 */
class SmokeAnalyticsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        startKoin {
            androidContext(this@SmokeAnalyticsApplication)
            modules(
                // data
                threadingModule,
                smokesDataModule,
                authenticationDataModule,
                preferencesDataModule,
                cravingsDataModule,
                wearDataModule,
                // domain
                smokesDomainModule,
                cravingsDomainModule,
                preferencesDomainModule,
                authenticationDomainModule,
                homeDomainModule,
                goalsDomainModule,
                // presentation
                homePresentationModule,
                settingsPresentationModule,
                goalsPresentationModule,
                devToolsPresentationModule,
                historyPresentationModule,
                authenticationPresentationModule,
                statsPresentationModule,
                // app
                appModule,
            )
        }
    }
}
