package com.feragusper.smokeanalytics

import com.feragusper.smokeanalytics.apps.web.BuildKonfig
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTracker
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLScriptElement

/**
 * Web [AnalyticsTracker]. When a GA4 measurement id is configured it loads gtag.js and forwards
 * events to Google Analytics; otherwise it logs to the console so events are still observable in
 * development. Screen views are sent as GA4 `page_view`/`screen_view` events.
 */
class WebAnalyticsTracker(
    private val measurementId: String = BuildKonfig.FIREBASE_MEASUREMENT_ID,
) : AnalyticsTracker {

    private val enabled = measurementId.isNotBlank()

    init {
        if (enabled) loadGtag(measurementId)
    }

    override fun screenView(screenName: String) {
        send("screen_view", mapOf("screen_name" to screenName))
    }

    override fun logEvent(name: String, params: Map<String, Any>) {
        send(name, params)
    }

    override fun setUserId(userId: String?) {
        if (enabled) {
            gtag("set", jsOf(mapOf("user_id" to (userId ?: ""))))
        }
    }

    private fun send(name: String, params: Map<String, Any>) {
        if (enabled) {
            gtag("event", name, jsOf(params))
        } else {
            console.debug("[analytics] $name", jsOf(params))
        }
    }

    private companion object {
        fun loadGtag(id: String) {
            // Bootstrap dataLayer + gtag shim (constant JS), then configure with the id.
            js("window.dataLayer = window.dataLayer || []; window.gtag = window.gtag || function(){window.dataLayer.push(arguments);};")
            gtag("js", js("new Date()"))
            gtag("config", id)
            val script = document.createElement("script") as HTMLScriptElement
            script.async = true
            script.src = "https://www.googletagmanager.com/gtag/js?id=$id"
            document.head?.appendChild(script)
        }
    }
}

/** Builds a plain JS object from a Kotlin map for gtag params. */
private fun jsOf(map: Map<String, Any>): dynamic {
    val obj = js("({})")
    map.forEach { (key, value) -> obj[key] = value }
    return obj
}

private fun gtag(vararg args: Any?) {
    val g = window.asDynamic().gtag
    if (g != null) g.apply(null, args)
}

private val console: dynamic get() = js("console")
