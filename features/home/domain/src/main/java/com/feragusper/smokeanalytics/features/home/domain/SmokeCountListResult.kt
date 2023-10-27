package com.feragusper.smokeanalytics.features.home.domain

data class SmokeCountListResult(
    val byToday: Int,
    val byWeek: Int,
    val byMonth: Int,
)