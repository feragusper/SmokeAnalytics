package com.feragusper.smokeanalytics.libraries.preferences.domain

data class UserPreferences(
    val packPrice: Double = 0.0,
    val cigarettesPerPack: Int = 20,
    val dayStartHour: Int = 6,
    val locationTrackingEnabled: Boolean = false,
    val accountTier: AccountTier = AccountTier.Free,
) {
    val cigarettePrice: Double
        get() = if (cigarettesPerPack > 0) packPrice / cigarettesPerPack else 0.0
}
