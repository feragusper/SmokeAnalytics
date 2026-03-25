package com.feragusper.smokeanalytics.libraries.preferences.data

import com.feragusper.smokeanalytics.libraries.preferences.domain.AccountTier
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences

data class UserPreferencesEntity(
    val packPrice: Double = 0.0,
    val cigarettesPerPack: Long = 20,
    val dayStartHour: Long = 6,
    val bedtimeHour: Long = 22,
    val manualDayStartEpochMillis: Long? = null,
    val locationTrackingEnabled: Boolean = false,
    val currencySymbol: String = "€",
    val accountTier: String = AccountTier.Free.name,
) {
    fun toDomain(): UserPreferences = UserPreferences(
        packPrice = packPrice,
        cigarettesPerPack = cigarettesPerPack.toInt(),
        dayStartHour = dayStartHour.toInt(),
        bedtimeHour = bedtimeHour.toInt(),
        manualDayStartEpochMillis = manualDayStartEpochMillis,
        locationTrackingEnabled = locationTrackingEnabled,
        currencySymbol = currencySymbol,
        accountTier = AccountTier.entries.firstOrNull { it.name == accountTier } ?: AccountTier.Free,
    )

    companion object {
        const val DOCUMENT = "preferences"
    }
}
