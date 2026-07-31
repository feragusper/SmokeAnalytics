package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeRelationship
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeTrigger
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.SetSmokeRelationshipUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Plain, Swift-friendly snapshot of the Home screen state. Uses primitives only (no Kotlin
 * `Instant`, enums or `List<Smoke>`) so SwiftUI can bind to it without interop friction.
 * [lastSmokeEpochMillis] is -1 when there is no recorded smoke.
 */
data class HomeSnapshot(
    val todayCount: Int,
    val weekCount: Int,
    val monthCount: Int,
    val lastSmokeEpochMillis: Long,
)

/** A selectable trigger tag with its display text ("☕ Coffee") for the relationship prompt. */
data class TriggerOptionDTO(val key: String, val display: String)

/**
 * Swift-facing entry point for the Home screen. Sign-in is owned by the Swift side (Google →
 * FirebaseAuth); GitLive's repositories here read the same `Firebase.auth.currentUser`. All calls
 * assume a signed-in user and throw otherwise (Swift catches via the `@Throws` bridge).
 */
class HomeFacade : KoinComponent {

    private val fetchCounts: FetchSmokeCountListUseCase by inject()
    private val addSmoke: AddSmokeUseCase by inject()
    private val setSmokeRelationship: SetSmokeRelationshipUseCase by inject()
    private val fetchPreferences: FetchUserPreferencesUseCase by inject()

    /** Returns the current aggregated counts for the signed-in user. */
    @Throws(Throwable::class)
    suspend fun load(): HomeSnapshot {
        val result = fetchCounts()
        return HomeSnapshot(
            todayCount = result.todaysSmokes.size,
            weekCount = result.countByWeek,
            monthCount = result.countByMonth,
            lastSmokeEpochMillis = result.lastSmoke?.date?.toEpochMilliseconds() ?: -1L,
        )
    }

    /** Logs a smoke at the current time and returns its id, so the caller can tag it. */
    @Throws(Throwable::class)
    suspend fun logSmoke(): String = addSmoke()

    /** The selectable trigger tags (built-ins minus hidden + the user's custom tags). */
    @Throws(Throwable::class)
    suspend fun triggerOptions(): List<TriggerOptionDTO> {
        val preferences = fetchPreferences()
        return SmokeTrigger.catalog(
            customTriggers = preferences.customTriggers,
            hiddenDefaultKeys = preferences.hiddenDefaultTriggers,
            iconOverrides = preferences.triggerIcons,
            labelOverrides = preferences.triggerLabels,
        ).map { TriggerOptionDTO(key = it.key, display = it.display) }
    }

    /** Tags a smoke with [tags]; an empty list records it as having no particular trigger. */
    @Throws(Throwable::class)
    suspend fun setRelationship(id: String, tags: List<String>) {
        val relationship = if (tags.isEmpty()) {
            SmokeRelationship.Skipped
        } else {
            SmokeRelationship.Tagged(tags.toSet())
        }
        setSmokeRelationship(id, relationship)
    }
}
