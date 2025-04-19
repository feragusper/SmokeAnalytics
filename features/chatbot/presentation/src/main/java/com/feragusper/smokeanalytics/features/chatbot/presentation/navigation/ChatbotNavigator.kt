package com.feragusper.smokeanalytics.features.chatbot.presentation.navigation

import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator

/**
 * Navigator for the Stats feature, managing navigation actions within the stats screen.
 *
 * This class encapsulates all navigation-related logic for the Stats feature,
 * ensuring consistent and maintainable navigation patterns.
 */
class ChatbotNavigator : MVINavigator {

    /**
     * Navigation actions for the Stats feature.
     *
     * Use these actions to navigate between different screens within the Stats flow.
     * For example, navigating from a list of statistics to a detailed view.
     */

    companion object {
        /**
         * The route for the Stats navigation graph.
         *
         * This is used to define the start of the Stats flow within the navigation system.
         */
        const val ROUTE = "chatbot_graph"

        /**
         * The start destination for the Stats navigation graph.
         *
         * This is the entry point of the Stats flow, typically the main statistics screen.
         */
        const val START = "chatbot"
    }
}
