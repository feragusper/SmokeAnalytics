package com.feragusper.smokeanalytics.features.stats.presentation.web

import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

/**
 * Returns the 2D context of the canvas with the given ID.
 *
 * @param canvasId The ID of the canvas.
 *
 * @return The 2D context of the canvas.
 */
fun canvas2dContext(canvasId: String): CanvasRenderingContext2D {
    val canvas = document.getElementById(canvasId) as? HTMLCanvasElement
        ?: error("Canvas not found: $canvasId")
    return canvas.getContext("2d") as CanvasRenderingContext2D
}