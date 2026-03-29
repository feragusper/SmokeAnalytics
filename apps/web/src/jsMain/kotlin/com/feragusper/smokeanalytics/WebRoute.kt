package com.feragusper.smokeanalytics

sealed class WebRoute {
    data object Home : WebRoute()
    data object Analytics : WebRoute()
    data object History : WebRoute()
    data object Coach : WebRoute()
    data object Settings : WebRoute()
    data object Auth : WebRoute()
}

internal fun WebRoute.toHash(): String = when (this) {
    WebRoute.Home -> "#/home"
    WebRoute.Analytics -> "#/analytics"
    WebRoute.History -> "#/history"
    WebRoute.Coach -> "#/coach"
    WebRoute.Settings -> "#/you"
    WebRoute.Auth -> "#/auth"
}

internal fun parseRouteFromHash(hash: String): WebRoute = when (hash.removePrefix("#")) {
    "/analytics", "/stats", "/map" -> WebRoute.Analytics
    "/history" -> WebRoute.History
    "/coach" -> WebRoute.Coach
    "/you", "/settings", "/about" -> WebRoute.Settings
    "/auth" -> WebRoute.Auth
    "/home", "/", "" -> WebRoute.Home
    else -> WebRoute.Home
}
