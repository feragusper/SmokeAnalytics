package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
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
