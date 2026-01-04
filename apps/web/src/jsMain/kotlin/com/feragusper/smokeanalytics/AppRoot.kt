package com.feragusper.smokeanalytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationWebScreen
import com.feragusper.smokeanalytics.features.authentication.presentation.createAuthenticationWebDependencies
import com.feragusper.smokeanalytics.features.history.presentation.HistoryWebDependencies
import com.feragusper.smokeanalytics.features.history.presentation.HistoryWebScreen
import com.feragusper.smokeanalytics.features.history.presentation.process.HistoryProcessHolder
import com.feragusper.smokeanalytics.features.home.presentation.web.HomeWebDependencies
import com.feragusper.smokeanalytics.features.home.presentation.web.HomeWebScreen
import com.feragusper.smokeanalytics.features.settings.presentation.web.SettingsWebScreen
import com.feragusper.smokeanalytics.features.settings.presentation.web.createSettingsWebDependencies
import com.feragusper.smokeanalytics.features.stats.presentation.web.StatsWebScreen
import com.feragusper.smokeanalytics.features.stats.presentation.web.createStatsWebDependencies

/**
 * The root composable for the web application.
 *
 * @param graph The dependency graph for the web application.
 */
@Composable
fun AppRoot(graph: WebAppGraph) {
    var tab by remember { mutableStateOf(WebTab.Home) }
    var route by remember { mutableStateOf(WebRoute.Tabs) }

    val homeDeps = remember(graph) {
        HomeWebDependencies(
            homeProcessHolder = graph.homeProcessHolder,
        )
    }

    val historyDeps = remember(graph) {
        HistoryWebDependencies(
            historyProcessHolder = HistoryProcessHolder(
                addSmokeUseCase = graph.addSmokeUseCase,
                editSmokeUseCase = graph.editSmokeUseCase,
                deleteSmokeUseCase = graph.deleteSmokeUseCase,
                fetchSmokesUseCase = graph.fetchSmokesUseCase,
                fetchSessionUseCase = graph.fetchSessionUseCase,
            )
        )
    }

    when (route) {
        WebRoute.Tabs -> {
            WebScaffold(
                tab = tab,
                onTabSelected = { tab = it },
            ) {
                when (tab) {
                    WebTab.Home -> HomeWebScreen(
                        deps = homeDeps,
                        onNavigateToHistory = { route = WebRoute.History },
                    )

                    WebTab.Stats -> {
                        val statsDeps = remember(graph) {
                            createStatsWebDependencies(
                                fetchSmokeStatsUseCase = graph.fetchSmokeStatsUseCase,
                            )
                        }
                        StatsWebScreen(
                            deps = statsDeps
                        )
                    }

                    WebTab.Settings -> {
                        val settingsDeps = remember(graph) {
                            createSettingsWebDependencies(
                                fetchSessionUseCase = graph.fetchSessionUseCase,
                                signOutUseCase = graph.signOutUseCase,
                            )
                        }

                        SettingsWebScreen(deps = settingsDeps)
                    }
                }
            }
        }

        WebRoute.Auth -> {
            val authDeps = remember(graph) {
                createAuthenticationWebDependencies(
                    fetchSessionUseCase = graph.fetchSessionUseCase,
                    signOutUseCase = graph.signOutUseCase,
                    signInWithGoogle = { /* no-op for now (handled by UI component) */ }
                )
            }


            AuthenticationWebScreen(
                deps = authDeps,
                onLoggedIn = {
                    route = WebRoute.Tabs
                    tab = WebTab.Home
                }
            )
        }

        WebRoute.History -> {
            HistoryWebScreen(
                deps = historyDeps,
                onNavigateUp = {
                    route = WebRoute.Tabs
                    tab = WebTab.Home
                },
                onNavigateToAuth = { route = WebRoute.Auth },
            )
        }
    }
}

/**
 * The dependency graph for the web application.
 */
private enum class WebRoute { Tabs, Auth, History }

/**
 * The tabs for the web application.
 */
enum class WebTab { Home, Stats, Settings }
