package com.feragusper.smokeanalytics.tile

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent

/**
 * Represents the intents (actions) that can be triggered in the Tile feature.
 * These intents are used to manage interactions with the tile view and
 * request data or update the UI accordingly.
 */
sealed class TileIntent : MVIIntent {

    /**
     * Represents the intent to fetch smoke data.
     * This intent can be triggered when the tile needs to update its displayed data.
     */
    object FetchSmokes : TileIntent()

    /**
     * Represents the intent to add a new smoke entry.
     * This is triggered when the user performs the "Add Smoke" action on the tile.
     */
    object AddSmoke : TileIntent()
}
