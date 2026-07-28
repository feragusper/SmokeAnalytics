package com.feragusper.smokeanalytics.features.settings.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.feragusper.smokeanalytics.features.settings.presentation.navigation.SettingsNavigator
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Standalone full-screen host for a settings sub-screen. Any flow that leaves the bottom-bar
 * shell runs as its own Activity so it gets a proper activity transition, a back button, and
 * no bottom navigation.
 */
class SettingsDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val section = runCatching {
            SettingsSection.valueOf(intent.getStringExtra(EXTRA_SECTION).orEmpty())
        }.getOrDefault(SettingsSection.ABOUT)

        setContent {
            SmokeAnalyticsTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val viewModel = koinViewModel<SettingsViewModel>()
                    val navController = rememberNavController()
                    viewModel.navigator = remember { SettingsNavigator(navController) }

                    SettingsDetailScreen(
                        section = section,
                        viewModel = viewModel,
                        onBack = { finish() },
                    )
                }
            }
        }
    }

    companion object {
        private const val EXTRA_SECTION = "settings_section"

        fun intent(context: Context, section: SettingsSection): Intent =
            Intent(context, SettingsDetailActivity::class.java)
                .putExtra(EXTRA_SECTION, section.name)
    }
}
