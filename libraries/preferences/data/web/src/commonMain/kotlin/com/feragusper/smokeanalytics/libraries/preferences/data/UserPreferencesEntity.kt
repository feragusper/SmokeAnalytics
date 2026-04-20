package com.feragusper.smokeanalytics.libraries.preferences.data

import com.feragusper.smokeanalytics.libraries.preferences.domain.AccountTier
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.preferences.domain.smokingGoalOrNull
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferencesEntity(
    val packPrice: Double = 0.0,
    val cigarettesPerPack: Int = 20,
    val dayStartHour: Int = 6,
    val bedtimeHour: Int = 22,
    val manualDayStartEpochMillis: Long? = null,
    val locationTrackingEnabled: Boolean = false,
    val currencySymbol: String = "€",
    val accountTier: String = AccountTier.Free.name,
    val activeGoalType: String? = null,
    val activeGoalMetricValue: Double? = null,
) {
    fun toDomain(): UserPreferences = UserPreferences(
        packPrice = packPrice,
        cigarettesPerPack = cigarettesPerPack,
        dayStartHour = dayStartHour,
        bedtimeHour = bedtimeHour,
        manualDayStartEpochMillis = manualDayStartEpochMillis,
        locationTrackingEnabled = locationTrackingEnabled,
        currencySymbol = currencySymbol,
        accountTier = AccountTier.entries.firstOrNull { it.name == accountTier } ?: AccountTier.Free,
        activeGoal = smokingGoalOrNull(
            type = activeGoalType,
            metricValue = activeGoalMetricValue,
        ),
    )

    companion object {
        const val DOCUMENT = "preferences"
        const val PACK_PRICE = "packPrice"
        const val CIGARETTES_PER_PACK = "cigarettesPerPack"
        const val DAY_START_HOUR = "dayStartHour"
        const val BEDTIME_HOUR = "bedtimeHour"
        const val MANUAL_DAY_START_EPOCH_MILLIS = "manualDayStartEpochMillis"
        const val LOCATION_TRACKING_ENABLED = "locationTrackingEnabled"
        const val CURRENCY_SYMBOL = "currencySymbol"
        const val ACCOUNT_TIER = "accountTier"
        const val ACTIVE_GOAL_TYPE = "activeGoalType"
        const val ACTIVE_GOAL_METRIC_VALUE = "activeGoalMetricValue"
    }
}
