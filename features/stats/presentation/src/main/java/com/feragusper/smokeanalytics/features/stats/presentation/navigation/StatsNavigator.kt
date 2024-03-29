package com.feragusper.smokeanalytics.features.stats.presentation.navigation

import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator

/**
 * Navigator for the Stats feature, managing navigation actions within the stats screen.
 */
class StatsNavigator : MVINavigator {

    companion object {
        const val ROUTE = "stats_graph"
        const val START = "stats"
    }

}