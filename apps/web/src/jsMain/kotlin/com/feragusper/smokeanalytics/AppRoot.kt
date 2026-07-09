package com.feragusper.smokeanalytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.libraries.design.i18n.AppLanguage
import com.feragusper.smokeanalytics.libraries.design.i18n.AppStrings
import com.feragusper.smokeanalytics.libraries.design.i18n.LocalAppLanguage
import com.feragusper.smokeanalytics.libraries.design.i18n.LocalStrings
import com.feragusper.smokeanalytics.apps.web.AnalyticsTab
import com.feragusper.smokeanalytics.apps.web.AnalyticsWebScreen
import com.feragusper.smokeanalytics.apps.web.MapWebScreen
import com.feragusper.smokeanalytics.apps.web.MapWebStateHolder
import com.feragusper.smokeanalytics.apps.web.SettingsAboutWebScreen
import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationWebDependencies
import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationWebScreen
import com.feragusper.smokeanalytics.features.goals.presentation.web.GoalsWebDependencies
import com.feragusper.smokeanalytics.features.goals.presentation.web.GoalsWebScreen
import com.feragusper.smokeanalytics.features.history.presentation.HistoryWebScreen
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryWebStore
import com.feragusper.smokeanalytics.features.home.presentation.web.HomeWebScreen
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeWebStore
import com.feragusper.smokeanalytics.features.settings.presentation.web.SettingsWebDependencies
import com.feragusper.smokeanalytics.features.stats.presentation.web.StatsPeriod
import com.feragusper.smokeanalytics.features.stats.presentation.web.StatsWebScreen
import com.feragusper.smokeanalytics.features.stats.presentation.web.toDomainPeriodType
import com.feragusper.smokeanalytics.features.stats.presentation.web.mvi.StatsIntent
import com.feragusper.smokeanalytics.features.stats.presentation.web.mvi.StatsWebStore
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeMapPeriod
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.mp.KoinPlatform.getKoin
import org.w3c.dom.events.Event
import kotlin.time.Clock

@Composable
fun AppRoot() {
    var language by remember { mutableStateOf(AppLanguage.initial()) }
    val strings = AppStrings.forLanguage(language)

    CompositionLocalProvider(
        LocalStrings provides strings,
        LocalAppLanguage provides language,
    ) {
        AppContent(
            language = language,
            onLanguageChange = {
                language = it
                AppLanguage.persist(it)
            },
        )
    }
}

@Composable
private fun AppContent(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    val strings = LocalStrings.current
    var route by remember { mutableStateOf(parseRouteFromHash(window.location.hash)) }
    var analyticsTab by remember { mutableStateOf(parseAnalyticsTabFromHash(window.location.hash)) }
    var statsPeriod by remember { mutableStateOf(StatsPeriod.WEEK) }
    var statsSelectedDate by remember {
        mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }

    DisposableEffect(Unit) {
        val handler: (Event) -> Unit = {
            val currentHash = window.location.hash
            val parsedRoute = parseRouteFromHash(currentHash)
            route = parsedRoute
            analyticsTab = parseAnalyticsTabFromHash(currentHash)
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

    val homeStore = remember { getKoin().get<HomeWebStore>() }
    val historyStore = remember { getKoin().get<HistoryWebStore>() }
    val authDeps = remember { getKoin().get<AuthenticationWebDependencies>() }
    val statsStore = remember { getKoin().get<StatsWebStore>() }
    val mapStateHolder = remember { getKoin().get<MapWebStateHolder>() }
    val settingsDeps = remember { getKoin().get<SettingsWebDependencies>() }
    val goalsDeps = remember { getKoin().get<GoalsWebDependencies>() }

    LaunchedEffect(route, language) {
        document.title = when (route) {
            WebRoute.Home -> strings.titleHome
            WebRoute.Analytics -> strings.titleAnalytics
            WebRoute.History -> strings.titleHistory
            WebRoute.Goals -> strings.titleGoals
            WebRoute.Settings -> strings.titleYou
            WebRoute.Auth -> strings.titleSignIn
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

                AnalyticsTab.Map -> mapStateHolder.refresh(
                    period = statsPeriod.toSmokeMapPeriod(),
                    selectedDate = statsSelectedDate,
                )
            }

            else -> Unit
        }
    }

    WebScaffold(
        route = route,
        onNavigate = ::navigateTo,
        language = language,
        onLanguageChange = onLanguageChange,
    ) {
        when (route) {
            WebRoute.Home -> HomeWebScreen(
                store = homeStore,
                onNavigateToHistory = { navigateTo(WebRoute.History) },
                onNavigateToGoals = { navigateTo(WebRoute.Goals) },
            )

            WebRoute.Analytics -> AnalyticsWebScreen(
                selectedTab = analyticsTab,
                selectedPeriod = statsPeriod,
                selectedDate = statsSelectedDate,
                onSelectTab = { analyticsTab = it },
                onPeriodChange = { statsPeriod = it },
                onDateChange = { statsSelectedDate = it },
                statsContent = {
                    StatsWebScreen(
                        store = statsStore,
                        currentPeriod = statsPeriod,
                        selectedDate = statsSelectedDate,
                        embedded = true,
                        onPeriodChange = { statsPeriod = it },
                        onDateChange = { statsSelectedDate = it },
                    )
                },
                mapContent = {
                    MapWebScreen(
                        stateHolder = mapStateHolder,
                        embedded = true,
                    )
                },
            )

            WebRoute.Settings -> SettingsAboutWebScreen(
                settingsDeps = settingsDeps,
                onShare = { shareSmokeAnalytics() },
            )

            WebRoute.Goals -> GoalsWebScreen(
                deps = goalsDeps,
                onNavigateBack = { navigateTo(WebRoute.Home) },
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

private fun StatsPeriod.toSmokeMapPeriod(): SmokeMapPeriod = when (this) {
    StatsPeriod.DAY -> SmokeMapPeriod.Day
    StatsPeriod.WEEK -> SmokeMapPeriod.Week
    StatsPeriod.MONTH -> SmokeMapPeriod.Month
    StatsPeriod.YEAR -> SmokeMapPeriod.Year
}
