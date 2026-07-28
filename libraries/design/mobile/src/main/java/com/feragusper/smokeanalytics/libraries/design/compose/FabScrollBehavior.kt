package com.feragusper.smokeanalytics.libraries.design.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

/**
 * Tracks vertical scroll direction to decide whether a FAB should be visible:
 * hidden while scrolling down (reading further into the list) and shown while
 * scrolling back up. Attach [connection] to the scrollable container through
 * `Modifier.nestedScroll(state.connection)` and gate the FAB on [isVisible].
 */
class FabScrollState internal constructor(private val hideThreshold: Float) {

    var isVisible by mutableStateOf(true)
        private set

    /** Force the FAB back to visible, e.g. when re-entering a screen. */
    fun show() {
        isVisible = true
    }

    val connection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val dy = available.y
            if (dy < -hideThreshold) {
                isVisible = false
            } else if (dy > hideThreshold) {
                isVisible = true
            }
            return Offset.Zero
        }
    }
}

@Composable
fun rememberFabScrollState(hideThreshold: Float = 3f): FabScrollState =
    remember { FabScrollState(hideThreshold) }
