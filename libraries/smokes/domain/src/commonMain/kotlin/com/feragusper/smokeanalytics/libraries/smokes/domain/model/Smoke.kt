// commonMain
package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import kotlinx.datetime.Instant

data class Smoke(
    val id: String,
    val date: Instant,
    val timeElapsedSincePreviousSmoke: Pair<Long, Long> = 0L to 0L,
)