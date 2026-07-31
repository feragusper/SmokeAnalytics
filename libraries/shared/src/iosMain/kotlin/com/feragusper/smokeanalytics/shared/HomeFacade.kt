package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeRelationship
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeTrigger
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.SetSmokeRelationshipUseCase
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
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
    val nickname: String,
    val quitReason: String,
    val currencySymbol: String,
    val cigarettePrice: Double,
    val lastMonthCount: Int,
    val untaggedTodayCount: Int,
    val oldestUntaggedTodayId: String,
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
    private val fetchSmokes: FetchSmokesUseCase by inject()
    private val addSmoke: AddSmokeUseCase by inject()
    private val setSmokeRelationship: SetSmokeRelationshipUseCase by inject()
    private val fetchPreferences: FetchUserPreferencesUseCase by inject()

    /** Returns the current aggregated counts + the profile/cost bits Home shows, like Android. */
    @Throws(Throwable::class)
    suspend fun load(): HomeSnapshot {
        val result = fetchCounts()
        val preferences = fetchPreferences()

        val untagged = result.todaysSmokes.filter { it.relationship.isPending }
        val oldestUntagged = untagged.minByOrNull { it.date }

        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now().toLocalDateTime(tz)
        val thisMonthStart = LocalDate(now.year, now.monthNumber, 1)
        val prevMonthStart = thisMonthStart.plus(DatePeriod(months = -1))
        val lastMonthCount = fetchSmokes(
            prevMonthStart.atStartOfDayIn(tz),
            thisMonthStart.atStartOfDayIn(tz),
        ).size

        return HomeSnapshot(
            todayCount = result.todaysSmokes.size,
            weekCount = result.countByWeek,
            monthCount = result.countByMonth,
            lastSmokeEpochMillis = result.lastSmoke?.date?.toEpochMilliseconds() ?: -1L,
            nickname = preferences.nickname,
            quitReason = preferences.quitReason,
            currencySymbol = preferences.currencySymbol,
            cigarettePrice = preferences.cigarettePrice,
            lastMonthCount = lastMonthCount,
            untaggedTodayCount = untagged.size,
            oldestUntaggedTodayId = oldestUntagged?.id ?: "",
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
