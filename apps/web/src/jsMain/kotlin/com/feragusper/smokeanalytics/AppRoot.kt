package com.feragusper.smokeanalytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.apps.web.AnalyticsTab
import com.feragusper.smokeanalytics.apps.web.AnalyticsWebScreen
import com.feragusper.smokeanalytics.apps.web.CoachWebScreen
import com.feragusper.smokeanalytics.apps.web.MapWebScreen
import com.feragusper.smokeanalytics.apps.web.MapWebStateHolder
import com.feragusper.smokeanalytics.apps.web.SettingsAboutWebScreen
import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationWebScreen
import com.feragusper.smokeanalytics.features.authentication.presentation.createAuthenticationWebDependencies
import com.feragusper.smokeanalytics.features.history.presentation.HistoryWebScreen
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryWebStore
import com.feragusper.smokeanalytics.features.history.presentation.process.HistoryProcessHolder
import com.feragusper.smokeanalytics.features.home.presentation.web.HomeWebScreen
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeWebStore
import com.feragusper.smokeanalytics.features.settings.presentation.web.createSettingsWebDependencies
import com.feragusper.smokeanalytics.features.stats.presentation.web.StatsPeriod
import com.feragusper.smokeanalytics.features.stats.presentation.web.StatsWebScreen
import com.feragusper.smokeanalytics.features.stats.presentation.web.toDomainPeriodType
import com.feragusper.smokeanalytics.features.stats.presentation.web.createStatsWebDependencies
import com.feragusper.smokeanalytics.features.stats.presentation.web.mvi.StatsIntent
import com.feragusper.smokeanalytics.features.stats.presentation.web.mvi.StatsWebStore
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.w3c.dom.events.Event

@Composable
fun AppRoot(graph: WebAppGraph) {
    var route by remember { mutableStateOf(parseRouteFromHash(window.location.hash)) }
    var analyticsTab by remember { mutableStateOf(AnalyticsTab.Trends) }
    var statsPeriod by remember { mutableStateOf(StatsPeriod.WEEK) }
    var statsSelectedDate by remember {
        mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }

    DisposableEffect(Unit) {
        val handler: (Event) -> Unit = {
            val parsedRoute = parseRouteFromHash(window.location.hash)
            route = parsedRoute
            val canonicalHash = parsedRoute.toHash()
            if (window.location.hash != canonicalHash) {
                window.location.hash = canonicalHash
            }
        }
        window.addEventListener("hashchange", handler)
        onDispose { window.removeEventListener("hashchange", handler) }
    }

    LaunchedEffect(Unit) {
        val canonicalHash = route.toHash()
        if (window.location.hash != canonicalHash) {
            window.location.hash = canonicalHash
        }
    }

    val homeStore = remember(graph) { HomeWebStore(processHolder = graph.homeProcessHolder) }
    val historyStore = remember(graph) {
        HistoryWebStore(
            HistoryProcessHolder(
                addSmokeUseCase = graph.addSmokeUseCase,
                editSmokeUseCase = graph.editSmokeUseCase,
                deleteSmokeUseCase = graph.deleteSmokeUseCase,
                fetchSmokesUseCase = graph.fetchSmokesUseCase,
                fetchSessionUseCase = graph.fetchSessionUseCase,
                fetchUserPreferencesUseCase = graph.fetchUserPreferencesUseCase,
                locationCaptureService = graph.locationCaptureService,
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
        createStatsWebDependencies(
            fetchSmokeStatsUseCase = graph.fetchSmokeStatsUseCase,
            fetchUserPreferencesUseCase = graph.fetchUserPreferencesUseCase,
        )
    }
    val statsStore = remember(statsDeps) { StatsWebStore(processHolder = statsDeps.processHolder) }
    val mapStateHolder = remember(graph) {
        MapWebStateHolder(
            fetchSmokesUseCase = graph.fetchSmokesUseCase,
            fetchUserPreferencesUseCase = graph.fetchUserPreferencesUseCase,
        )
    }
    val settingsDeps = remember(graph) {
        createSettingsWebDependencies(
            fetchSessionUseCase = graph.fetchSessionUseCase,
            signOutUseCase = graph.signOutUseCase,
            fetchUserPreferencesUseCase = graph.fetchUserPreferencesUseCase,
            updateUserPreferencesUseCase = graph.updateUserPreferencesUseCase,
            fetchSmokesUseCase = graph.fetchSmokesUseCase,
        )
    }

    LaunchedEffect(route) {
        document.title = when (route) {
            WebRoute.Home -> "Smoke Analytics | Home"
            WebRoute.Analytics -> "Smoke Analytics | Analytics & Map"
            WebRoute.History -> "Smoke Analytics | History"
            WebRoute.Coach -> "Smoke Analytics | The Guide"
            WebRoute.Settings -> "Smoke Analytics | You"
            WebRoute.Auth -> "Smoke Analytics | Sign in"
        }
    }

    LaunchedEffect(route, analyticsTab, statsPeriod, statsSelectedDate) {
        when (route) {
            WebRoute.Home -> {
                val homeState = homeStore.state.value
                val hasCachedData = homeState.lastSmoke != null || homeState.timeSinceLastCigarette != null
                homeStore.send(if (hasCachedData) HomeIntent.RefreshFetchSmokes else HomeIntent.FetchSmokes)
            }

            WebRoute.History -> {
                historyStore.send(HistoryIntent.FetchSmokes(historyStore.state.value.selectedDate))
            }

            WebRoute.Analytics -> when (analyticsTab) {
                AnalyticsTab.Trends -> statsStore.send(
                    StatsIntent.LoadStats(
                        year = statsSelectedDate.year,
                        month = statsSelectedDate.monthNumber,
                        day = statsSelectedDate.dayOfMonth,
                        period = statsPeriod.toDomainPeriodType(),
                    )
                )

                AnalyticsTab.Map -> mapStateHolder.refresh()
            }

            else -> Unit
        }
    }

    WebScaffold(
        route = route,
        onNavigate = ::navigateTo,
    ) {
        when (route) {
            WebRoute.Home -> HomeWebScreen(
                store = homeStore,
                onNavigateToHistory = { navigateTo(WebRoute.History) },
                onNavigateToGoals = { navigateTo(WebRoute.Settings) },
            )

            WebRoute.Analytics -> AnalyticsWebScreen(
                selectedTab = analyticsTab,
                onSelectTab = { analyticsTab = it },
                statsContent = {
                    StatsWebScreen(
                        store = statsStore,
                        currentPeriod = statsPeriod,
                        selectedDate = statsSelectedDate,
                        onPeriodChange = { statsPeriod = it },
                        onDateChange = { statsSelectedDate = it },
                    )
                },
                mapContent = {
                    MapWebScreen(stateHolder = mapStateHolder)
                },
            )

            WebRoute.Coach -> CoachWebScreen(
                chatbotUseCase = graph.chatbotUseCase,
            )

            WebRoute.Settings -> SettingsAboutWebScreen(
                settingsDeps = settingsDeps,
                onShare = { shareSmokeAnalytics() },
            )

            WebRoute.Auth -> AuthenticationWebScreen(
                deps = authDeps,
                onLoggedIn = { navigateTo(WebRoute.Home) }
            )

            WebRoute.History -> HistoryWebScreen(
                store = historyStore,
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

private suspend fun shareSmokeAnalytics() {
    val shareText = "Smoke Analytics helps you track smokes, streaks, and spending."
    val shareUrl = window.location.origin
    val nav = js("navigator")

    try {
        if (js("typeof navigator.share === 'function'") as Boolean) {
            nav.share(js("{ title: 'Smoke Analytics', text: shareText, url: shareUrl }"))
        } else if (js("navigator.clipboard && navigator.clipboard.writeText") as Boolean) {
            nav.clipboard.writeText("$shareText $shareUrl")
        }
    } catch (_: Throwable) {
    }
}
