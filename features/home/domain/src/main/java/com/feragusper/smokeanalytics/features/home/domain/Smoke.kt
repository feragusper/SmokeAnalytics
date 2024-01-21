package com.feragusper.smokeanalytics.features.home.domain

import java.util.Date

data class Smoke(val date: Date, val timeElapsedSincePreviousSmoke: Pair<Long, Long>)