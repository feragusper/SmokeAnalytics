package com.feragusper.smokeanalytics.libraries.smokes.domain

import java.util.Date

interface SmokeRepository {
    suspend fun addSmoke(date: Date)
    suspend fun fetchSmokes(): List<Smoke>
    suspend fun editSmoke(id: String, date: Date)
    suspend fun deleteSmoke(id: String)
}
