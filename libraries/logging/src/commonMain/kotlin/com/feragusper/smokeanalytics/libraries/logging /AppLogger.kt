package com.feragusper.smokeanalytics.libraries.logging

import co.touchlab.kermit.Logger

/**
 * Represents an app logger.
 */
object AppLogger {

    private val log = Logger.withTag("SmokeAnalytics")

    /**
     * Logs a debug message.
     *
     * @param message The message to log.
     */
    fun d(message: () -> String) = log.d(message())

    /**
     * Logs an info message.
     *
     * @param message The message to log.
     */
    fun i(message: () -> String) = log.i(message())

    /**
     * Logs a warning message.
     *
     * @param message The message to log.
     */
    fun w(message: () -> String) = log.w(message())

    /**
     * Logs an error message.
     *
     * @param message The message to log.
     */
    fun e(message: () -> String) = log.e(message())
}