package com.feragusper.smokeanalytics.libraries.smokes.domain

import java.time.LocalDateTime

data class Smoke(
    val id: String,
    val date: LocalDateTime,
    val timeElapsedSincePreviousSmoke: Pair<Long, Long>
)