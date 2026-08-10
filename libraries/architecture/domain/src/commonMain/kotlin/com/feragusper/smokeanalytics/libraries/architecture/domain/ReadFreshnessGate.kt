package com.feragusper.smokeanalytics.libraries.architecture.domain

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * Decides whether a screen load should read from the server or from the local cache, so the app
 * behaves offline-first and only spends Firestore server reads when it is actually worth it.
 *
 * Policy for [resolveSource]:
 * - `forceRefresh` (a pull-to-refresh or a cold start with no cache) → [DataSource.SERVER].
 * - never synced from the server yet, or the last server sync is older than [ttl] → [DataSource.SERVER].
 * - otherwise → [DataSource.CACHE] (free, offline).
 *
 * Callers must report a completed server load via [markServerLoad] so the [ttl] window restarts.
 *
 * State is in-memory: after a cold start the first load is always a server read (which repopulates
 * the cache), and every subsequent read within [ttl] is served from the cache for free.
 *
 * Registered as a singleton so the window is shared across the (per-request) callers that consult it.
 */
class ReadFreshnessGate(
    private val ttl: Duration = DEFAULT_TTL,
    private val clock: Clock = Clock.System,
) {

    private var lastServerLoadMillis: Long? = null

    /**
     * @param forceRefresh true for an explicit user refresh or a cold start; forces a server read.
     * @return the source the caller should use for every read in this load.
     */
    fun resolveSource(forceRefresh: Boolean): DataSource {
        if (forceRefresh) return DataSource.SERVER
        val last = lastServerLoadMillis ?: return DataSource.SERVER
        val elapsed = clock.now().toEpochMilliseconds() - last
        return if (elapsed >= ttl.inWholeMilliseconds) DataSource.SERVER else DataSource.CACHE
    }

    /** Restarts the [ttl] window. Call after a load that read from the server succeeded. */
    fun markServerLoad() {
        lastServerLoadMillis = clock.now().toEpochMilliseconds()
    }

    private companion object {
        /** How long a server sync stays "fresh" before a background load hits the server again. */
        val DEFAULT_TTL: Duration = 4.hours
    }
}
