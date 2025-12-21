@file:JsModule("chart.js/auto")
@file:JsNonModule

package com.feragusper.smokeanalytics.features.stats.presentation.web

import org.w3c.dom.CanvasRenderingContext2D

external class Chart(
    ctx: CanvasRenderingContext2D,
    config: dynamic,
) {
    fun destroy()
}