package com.feragusper.smokeanalytics.libraries.architecture.domain

/**
 * Where a repository read should be served from.
 *
 * Firestore bills every document a query reads *from the server*. Reads served from the
 * on-device persistent cache are free and work offline, so an offline-first screen should
 * default to [CACHE] and only hit [SERVER] on an explicit refresh or on a periodic sync.
 *
 * - [CACHE]   Read only from the local cache. Never billed, works offline. Returns empty/absent
 *             when nothing is cached yet (callers must guarantee a prior [SERVER] sync, e.g. via
 *             [ReadFreshnessGate]).
 * - [SERVER]  Force a server read. Billed, refreshes the cache, requires connectivity.
 * - [DEFAULT] Firestore's default: server when online, cache when offline. Billed when online.
 *             Kept for callers that have not opted into the cache-first policy.
 */
enum class DataSource {
    CACHE,
    SERVER,
    DEFAULT,
}
