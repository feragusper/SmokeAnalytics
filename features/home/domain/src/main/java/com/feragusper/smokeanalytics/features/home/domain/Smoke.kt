package com.feragusper.smokeanalytics.features.home.domain

import java.util.Date

data class Smoke(
    val id: String,
    val date: Date,
    val timeElapsedSincePreviousSmoke: Pair<Long, Long>
)