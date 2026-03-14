package com.feragusper.smokeanalytics.libraries.preferences.data

import com.feragusper.smokeanalytics.libraries.preferences.domain.AccountTier
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferencesEntity(
    val packPrice: Double = 0.0,
    val cigarettesPerPack: Int = 20,
    val dayStartHour: Int = 6,
    val locationTrackingEnabled: Boolean = false,
    val accountTier: String = AccountTier.Free.name,
) {
    fun toDomain(): UserPreferences = UserPreferences(
        packPrice = packPrice,
        cigarettesPerPack = cigarettesPerPack,
        dayStartHour = dayStartHour,
        locationTrackingEnabled = locationTrackingEnabled,
        accountTier = AccountTier.entries.firstOrNull { it.name == accountTier } ?: AccountTier.Free,
    )

    companion object {
        const val DOCUMENT = "preferences"
    }
}
