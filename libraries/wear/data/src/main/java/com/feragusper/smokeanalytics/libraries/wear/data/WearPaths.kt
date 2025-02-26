package com.feragusper.smokeanalytics.libraries.wear.data

// Object containing constants for various Wear OS communication paths
object WearPaths {
    // Path for fetching smoke data
    const val SMOKE_DATA = "/smoke_data"

    // Path to request the list of smokes from the mobile device
    const val REQUEST_SMOKES = "/smokes-request"

    // Path to add a smoke record
    const val ADD_SMOKE = "/add-smoke"

    // Constants representing the different smoke count metrics
    const val SMOKE_COUNT_TODAY = "smoke_count_today" // Count of smokes for today
    const val SMOKE_COUNT_WEEK = "smoke_count_week" // Count of smokes for the current week
    const val SMOKE_COUNT_MONTH = "smoke_count_month" // Count of smokes for the current month

    // Path to get the timestamp of the last smoke
    const val LAST_SMOKE_TIMESTAMP = "last_smoke_timestamp"
}
