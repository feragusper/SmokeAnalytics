package com.feragusper.smokeanalytics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.feragusper.smokeanalytics.features.goals.presentation.GoalsConfigureView
import com.feragusper.smokeanalytics.features.goals.presentation.GoalsViewModel
import com.feragusper.smokeanalytics.features.goals.presentation.navigation.GoalsNavigator
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsScreen
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTracker
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * Standalone full-screen goal editor. Any flow that leaves the bottom-bar shell runs as its
 * own Activity so it gets a proper activity transition instead of an in-shell nav slide.
 */
class GoalsConfigureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmokeAnalyticsTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val analytics = koinInject<AnalyticsTracker>()
                    LaunchedEffect(Unit) {
                        analytics.screenView(AnalyticsScreen.GOALS_CONFIGURE)
                    }

                    val viewModel = koinViewModel<GoalsViewModel>()
                    viewModel.navigator = remember { GoalsNavigator(navigateBack = { finish() }) }

                    GoalsConfigureView(
                        viewModel = viewModel,
                        navigateBack = { finish() },
                    )
                }
            }
        }
    }
}
