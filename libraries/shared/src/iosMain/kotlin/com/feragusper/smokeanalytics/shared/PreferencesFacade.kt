package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeTrigger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The user-editable subset of preferences, flattened for SwiftUI. Goal, triggers and other stored
 * state are preserved on save (see [PreferencesFacade.save]).
 */
data class PreferencesDTO(
    val nickname: String,
    val quitReason: String,
    val packPrice: Double,
    val cigarettesPerPack: Int,
    val currencySymbol: String,
    val dayStartHour: Int,
    val bedtimeHour: Int,
    val weekStartsMonday: Boolean,
    val use24HourClock: Boolean,
    val locationTrackingEnabled: Boolean,
)

/** A trigger tag for the management screen: built-in (hideable) or custom (deletable). */
data class TagDTO(val key: String, val display: String, val isCustom: Boolean, val hidden: Boolean)

/** Swift-facing entry point for the settings form. */
class PreferencesFacade : KoinComponent {

    private val fetchPreferences: FetchUserPreferencesUseCase by inject()
    private val updatePreferences: UpdateUserPreferencesUseCase by inject()

    @Throws(Throwable::class)
    suspend fun load(): PreferencesDTO = with(fetchPreferences()) {
        PreferencesDTO(
            nickname = nickname,
            quitReason = quitReason,
            packPrice = packPrice,
            cigarettesPerPack = cigarettesPerPack,
            currencySymbol = currencySymbol,
            dayStartHour = dayStartHour,
            bedtimeHour = bedtimeHour,
            weekStartsMonday = weekStartsMonday,
            use24HourClock = use24HourClock,
            locationTrackingEnabled = locationTrackingEnabled,
        )
    }

    // --- Trigger tag management -----------------------------------------------------------

    /** All trigger tags: built-ins (with their hidden state) + the user's custom tags. */
    @Throws(Throwable::class)
    suspend fun tags(): List<TagDTO> {
        val p = fetchPreferences()
        val builtins = SmokeTrigger.entries.map {
            val label = p.triggerLabels[it.key] ?: it.defaultLabel
            val icon = p.triggerIcons[it.key] ?: it.defaultIcon
            TagDTO(key = it.key, display = "$icon $label", isCustom = false, hidden = it.key in p.hiddenDefaultTriggers)
        }
        val customs = p.customTriggers.map {
            TagDTO(key = it, display = p.triggerIcons[it]?.let { icon -> "$icon $it" } ?: it, isCustom = true, hidden = false)
        }
        return builtins + customs
    }

    @Throws(Throwable::class)
    suspend fun addCustomTag(label: String) {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return
        val p = fetchPreferences()
        if (p.customTriggers.any { it.equals(trimmed, ignoreCase = true) }) return
        updatePreferences(p.copy(customTriggers = p.customTriggers + trimmed))
    }

    @Throws(Throwable::class)
    suspend fun removeCustomTag(key: String) {
        val p = fetchPreferences()
        updatePreferences(p.copy(customTriggers = p.customTriggers.filterNot { it == key }))
    }

    @Throws(Throwable::class)
    suspend fun setBuiltinHidden(key: String, hidden: Boolean) {
        val p = fetchPreferences()
        val next = if (hidden) p.hiddenDefaultTriggers + key else p.hiddenDefaultTriggers - key
        updatePreferences(p.copy(hiddenDefaultTriggers = next))
    }

    /** Persists the edited fields, preserving everything else on the stored preferences. */
    @Throws(Throwable::class)
    suspend fun save(dto: PreferencesDTO) {
        updatePreferences(
            fetchPreferences().copy(
                nickname = dto.nickname,
                quitReason = dto.quitReason,
                packPrice = dto.packPrice,
                cigarettesPerPack = dto.cigarettesPerPack,
                currencySymbol = dto.currencySymbol,
                dayStartHour = dto.dayStartHour,
                bedtimeHour = dto.bedtimeHour,
                weekStartsMonday = dto.weekStartsMonday,
                use24HourClock = dto.use24HourClock,
                locationTrackingEnabled = dto.locationTrackingEnabled,
            )
        )
    }
}
