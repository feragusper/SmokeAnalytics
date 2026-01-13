package com.feragusper.smokeanalytics

import com.feragusper.smokeanalytics.libraries.design.SmokeWebTheme
import org.jetbrains.compose.web.renderComposable

fun main() {
    FirebaseWebInit.init()
    val graph = WebAppGraph.create()

    renderComposable(rootElementId = "root") {
        SmokeWebTheme {
            AppRoot(graph)
        }
    }
}