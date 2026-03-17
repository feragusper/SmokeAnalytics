package com.feragusper.smokeanalytics.libraries.preferences.domain

data class UserPreferences(
    val packPrice: Double = 0.0,
    val cigarettesPerPack: Int = 20,
    val dayStartHour: Int = 6,
    val manualDayStartEpochMillis: Long? = null,
    val locationTrackingEnabled: Boolean = false,
    val currencySymbol: String = "€",
    val accountTier: AccountTier = AccountTier.Free,
) {
    val cigarettePrice: Double
        get() = if (cigarettesPerPack > 0) packPrice / cigarettesPerPack else 0.0
}

fun Double.formatMoney(symbol: String): String {
    val cents = (this * 100).toInt()
    val whole = cents / 100
    val fraction = (cents % 100).toString().padStart(2, '0')
    return "$symbol$whole.$fraction"
}
