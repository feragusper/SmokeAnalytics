package com.feragusper.smokeanalytics

sealed class WebRoute {
    data object Home : WebRoute()
    data object Analytics : WebRoute()
    data object History : WebRoute()
    data object Settings : WebRoute()
    data object Auth : WebRoute()
}

internal fun WebRoute.toHash(): String = when (this) {
    WebRoute.Home -> "#/home"
    WebRoute.Analytics -> "#/analytics"
    WebRoute.History -> "#/history"
    WebRoute.Settings -> "#/you"
    WebRoute.Auth -> "#/auth"
}

internal fun parseRouteFromHash(hash: String): WebRoute = when (hash.removePrefix("#")) {
    "/analytics", "/stats", "/map" -> WebRoute.Analytics
    "/history" -> WebRoute.History
    "/coach" -> WebRoute.Home
    "/you", "/settings", "/about" -> WebRoute.Settings
    "/auth" -> WebRoute.Auth
    "/home", "/", "" -> WebRoute.Home
    else -> WebRoute.Home
}

internal fun parseAnalyticsTabFromHash(hash: String) = when (hash.removePrefix("#")) {
    "/map" -> com.feragusper.smokeanalytics.apps.web.AnalyticsTab.Map
    else -> com.feragusper.smokeanalytics.apps.web.AnalyticsTab.Trends
}
