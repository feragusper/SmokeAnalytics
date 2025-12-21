// commonMain
package com.feragusper.smokeanalytics.libraries.smokes.domain.repository

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeCount
import kotlinx.datetime.Instant

interface SmokeRepository {

    suspend fun addSmoke(timestamp: Instant)

    suspend fun fetchSmokes(
        start: Instant? = null,
        end: Instant? = null,
    ): List<Smoke>

    suspend fun fetchSmokeCount(): SmokeCount

    suspend fun editSmoke(id: String, timestamp: Instant)

    suspend fun deleteSmoke(id: String)
}