package com.feragusper.smokeanalytics.libraries.smokes.domain

import java.time.LocalDateTime

interface SmokeRepository {
    suspend fun addSmoke(date: LocalDateTime)
    suspend fun fetchSmokes(date: LocalDateTime? = null): List<Smoke>
    suspend fun editSmoke(id: String, date: LocalDateTime)
    suspend fun deleteSmoke(id: String)
}
