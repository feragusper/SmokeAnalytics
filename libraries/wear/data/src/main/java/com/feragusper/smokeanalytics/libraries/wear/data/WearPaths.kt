package com.feragusper.smokeanalytics.libraries.wear.data

// Object containing constants for various Wear OS communication paths
object WearPaths {
    // Path for fetching smoke data
    const val SMOKE_DATA = "/smoke_data"

    // Path to request the list of smokes from the mobile device
    const val REQUEST_SMOKES = "/smokes-request"

    // Path to add a smoke record
    const val ADD_SMOKE = "/add-smoke"

    const val SMOKE_COUNT_TODAY = "smoke_count_today"
    const val AVERAGE_SMOKES_PER_DAY_WEEK = "average_smokes_per_day_week"
    const val TARGET_GAP_MINUTES = "target_gap_minutes"
    const val LAST_SMOKE_TIMESTAMP = "last_smoke_timestamp"
    const val SYNC_SENT_AT = "sync_sent_at"
}
