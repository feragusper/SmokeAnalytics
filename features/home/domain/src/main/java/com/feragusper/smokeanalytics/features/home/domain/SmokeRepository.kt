package com.feragusper.smokeanalytics.features.home.domain

interface SmokeRepository {
    suspend fun addSmoke()
    suspend fun fetchSmokes(): List<Smoke>
}
