@file:JsModule("chart.js/auto")
@file:JsNonModule

package com.feragusper.smokeanalytics.features.stats.presentation.web

import org.w3c.dom.CanvasRenderingContext2D

/**
 * Represents the Chart class.
 *
 * @param ctx The 2D context of the canvas.
 * @param config The configuration of the chart.
 */
external class Chart(
    ctx: CanvasRenderingContext2D,
    config: dynamic,
) {
    /**
     * Destroys the chart.
     */
    fun destroy()
}