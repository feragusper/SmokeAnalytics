package com.feragusper.smokeanalytics.features.home.domain

import java.util.Date

interface SmokeRepository {
    suspend fun addSmoke()
    suspend fun fetchSmokes(): List<Smoke>
    suspend fun editSmoke(id: String, date: Date)
}
