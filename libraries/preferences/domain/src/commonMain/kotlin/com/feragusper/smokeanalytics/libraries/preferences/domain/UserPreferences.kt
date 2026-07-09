package com.feragusper.smokeanalytics.libraries.preferences.domain

data class UserPreferences(
    val packPrice: Double = 0.0,
    val cigarettesPerPack: Int = 20,
    val dayStartHour: Int = 6,
    val bedtimeHour: Int = 22,
    val manualDayStartEpochMillis: Long? = null,
    val locationTrackingEnabled: Boolean = false,
    val currencySymbol: String = "€",
    val accountTier: AccountTier = AccountTier.Free,
    val activeGoal: SmokingGoal? = null,
    /** User-created trigger tags (labels), shown alongside the built-in defaults. */
    val customTriggers: List<String> = emptyList(),
    /** Built-in trigger keys the user hid from the prompt. */
    val hiddenDefaultTriggers: Set<String> = emptySet(),
    /** Emoji per trigger key: overrides a built-in's default icon or gives a custom tag one. */
    val triggerIcons: Map<String, String> = emptyMap(),
    /** Display name per trigger key: renames a tag without changing the key stored on smokes. */
    val triggerLabels: Map<String, String> = emptyMap(),
    /** Optional nickname shown in the Home greeting ("Good morning, <nickname>"). */
    val nickname: String = "",
    /** Optional personal reason to cut down, surfaced on the craving cards as a reminder. */
    val quitReason: String = "",
) {
    val cigarettePrice: Double
        get() = if (cigarettesPerPack > 0) packPrice / cigarettesPerPack else 0.0

    val awakeMinutesPerDay: Int
        get() {
            val awakeHours = (bedtimeHour - dayStartHour).mod(24).takeIf { it > 0 } ?: 16
            return awakeHours * 60
        }
}

fun Double.formatMoney(symbol: String): String {
    val cents = (this * 100).toInt()
    val whole = cents / 100
    val fraction = (cents % 100).toString().padStart(2, '0')
    return "$symbol$whole.$fraction"
}
