package com.feragusper.smokeanalytics.libraries.smokes.domain

data class SmokeCount(
    val today: List<Smoke>,
    val week: Int,
    val month: Int,
    val lastSmoke: Smoke?,
)