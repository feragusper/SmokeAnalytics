package com.feragusper.smokeanalytics

sealed class WebRoute {
    data object Home : WebRoute()
    data object Stats : WebRoute()
    data object Settings : WebRoute()
    data object History : WebRoute()
    data object Map : WebRoute()
    data object About : WebRoute()
    data object Auth : WebRoute()
}

internal fun WebRoute.toHash(): String = when (this) {
    WebRoute.Home -> "#/home"
    WebRoute.Stats -> "#/stats"
    WebRoute.Settings -> "#/settings"
    WebRoute.History -> "#/history"
    WebRoute.Map -> "#/map"
    WebRoute.About -> "#/about"
    WebRoute.Auth -> "#/auth"
}

internal fun parseRouteFromHash(hash: String): WebRoute = when (hash.removePrefix("#")) {
    "/stats" -> WebRoute.Stats
    "/settings" -> WebRoute.Settings
    "/history" -> WebRoute.History
    "/map" -> WebRoute.Map
    "/about" -> WebRoute.About
    "/auth" -> WebRoute.Auth
    "/home", "/", "" -> WebRoute.Home
    else -> WebRoute.Home
}
