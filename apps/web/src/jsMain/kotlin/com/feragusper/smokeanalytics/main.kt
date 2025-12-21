package com.feragusper.smokeanalytics

import org.jetbrains.compose.web.renderComposable

/**
 * The main entry point for the web application.
 */
fun main() {
    FirebaseWebInit.init()
    val graph = WebAppGraph.create()

    renderComposable(rootElementId = "root") {
        AppRoot(graph)
    }
}