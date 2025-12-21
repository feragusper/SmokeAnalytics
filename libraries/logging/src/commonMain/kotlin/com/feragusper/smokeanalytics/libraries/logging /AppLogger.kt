package com.feragusper.smokeanalytics.libraries.logging

import co.touchlab.kermit.Logger

object AppLogger {

    private val log = Logger.withTag("SmokeAnalytics")

    fun d(message: () -> String) = log.d(message())
    fun i(message: () -> String) = log.i(message())
    fun w(message: () -> String) = log.w(message())
    fun e(message: () -> String) = log.e(message())
}