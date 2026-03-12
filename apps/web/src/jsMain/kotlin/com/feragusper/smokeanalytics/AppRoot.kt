package com.feragusper.smokeanalytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event

@Composable
fun AppRoot(graph: WebAppGraph) {
    var route by remember {
        mutableStateOf(parseRouteFromHash(window.location.hash))
    }

    DisposableEffect(Unit) {
        val handler: (Event) -> Unit = {
            route = parseRouteFromHash(window.location.hash)
        }
        window.addEventListener("hashchange", handler)
        onDispose { window.removeEventListener("hashchange", handler) }
    }

    LaunchedEffect(route) {
        document.title = when (route) {
            WebRoute.Home -> "Smoke Analytics | Home"
            WebRoute.History -> "Smoke Analytics | History"
            WebRoute.Stats -> "Smoke Analytics | Stats"
            WebRoute.Settings -> "Smoke Analytics | Settings"
            WebRoute.Auth -> "Smoke Analytics | Sign in"
        }
    }

    val homeDeps = remember(graph) {
        HomeWebDependencies(homeProcessHolder = graph.homeProcessHolder)
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

    val authDeps = remember(graph) {
        createAuthenticationWebDependencies(
            fetchSessionUseCase = graph.fetchSessionUseCase,
            signOutUseCase = graph.signOutUseCase,
            signInWithGoogle = { }
        )
    }

    val statsDeps = remember(graph) {
        createStatsWebDependencies(fetchSmokeStatsUseCase = graph.fetchSmokeStatsUseCase)
    }

    val settingsDeps = remember(graph) {
        createSettingsWebDependencies(
            fetchSessionUseCase = graph.fetchSessionUseCase,
            signOutUseCase = graph.signOutUseCase,
        )
    }

    WebScaffold(
        route = route,
        onNavigate = ::navigateTo,
    ) {
        when (route) {
            WebRoute.Home -> HomeWebScreen(
                deps = homeDeps,
                onNavigateToHistory = { navigateTo(WebRoute.History) },
            )

            WebRoute.Stats -> StatsWebScreen(deps = statsDeps)

            WebRoute.Settings -> SettingsWebScreen(deps = settingsDeps)

            WebRoute.Auth -> AuthenticationWebScreen(
                deps = authDeps,
                onLoggedIn = { navigateTo(WebRoute.Home) }
            )

            WebRoute.History -> HistoryWebScreen(
                deps = historyDeps,
                onNavigateUp = { navigateTo(WebRoute.Home) },
                onNavigateToAuth = { navigateTo(WebRoute.Auth) },
            )
        }
    }
}

private fun navigateTo(route: WebRoute) {
    val target = route.toHash()
    if (window.location.hash != target) {
        window.location.hash = target
    }
}
