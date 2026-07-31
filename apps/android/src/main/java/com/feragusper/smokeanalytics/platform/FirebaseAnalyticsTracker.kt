package com.feragusper.smokeanalytics.platform

import android.os.Bundle
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTracker
import com.google.firebase.analytics.FirebaseAnalytics

/** Firebase Analytics implementation of [AnalyticsTracker] for Android. */
class FirebaseAnalyticsTracker(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AnalyticsTracker {

    override fun screenView(screenName: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }

    override fun logEvent(name: String, params: Map<String, Any>) {
        firebaseAnalytics.logEvent(name) {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> param(key, value)
                    is Int -> param(key, value.toLong())
                    is Long -> param(key, value)
                    is Double -> param(key, value)
                    is Float -> param(key, value.toDouble())
                    is Boolean -> param(key, value.toString())
                    else -> param(key, value.toString())
                }
            }
        }
    }

    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }
}

/** Minimal inline event builder to avoid pulling firebase-analytics-ktx just for the DSL. */
private inline fun FirebaseAnalytics.logEvent(name: String, block: Bundle.() -> Unit) {
    logEvent(name, Bundle().apply(block))
}

private fun Bundle.param(key: String, value: String) = putString(key, value)
private fun Bundle.param(key: String, value: Long) = putLong(key, value)
private fun Bundle.param(key: String, value: Double) = putDouble(key, value)
