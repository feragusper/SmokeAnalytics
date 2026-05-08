package com.feragusper.smokeanalytics.libraries.preferences.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class UserPreferencesTest {

    @Test
    fun cigarettePrice_normalPackPrice() {
        val prefs = UserPreferences(packPrice = 10.0, cigarettesPerPack = 20)
        assertEquals(0.5, prefs.cigarettePrice)
    }

    @Test
    fun cigarettePrice_zeroCigarettesPerPack_returnsZero() {
        val prefs = UserPreferences(packPrice = 10.0, cigarettesPerPack = 0)
        assertEquals(0.0, prefs.cigarettePrice)
    }

    @Test
    fun cigarettePrice_zeroPackPrice_returnsZero() {
        val prefs = UserPreferences(packPrice = 0.0, cigarettesPerPack = 20)
        assertEquals(0.0, prefs.cigarettePrice)
    }

    @Test
    fun awakeMinutesPerDay_normalRange() {
        val prefs = UserPreferences(dayStartHour = 6, bedtimeHour = 22)
        assertEquals(960, prefs.awakeMinutesPerDay) // 16h * 60
    }

    @Test
    fun awakeMinutesPerDay_bedtimeBeforeDayStart_wrapsAround() {
        val prefs = UserPreferences(dayStartHour = 22, bedtimeHour = 6)
        assertEquals(480, prefs.awakeMinutesPerDay) // 8h * 60
    }

    @Test
    fun awakeMinutesPerDay_sameHour_defaultsFallback() {
        val prefs = UserPreferences(dayStartHour = 8, bedtimeHour = 8)
        assertEquals(960, prefs.awakeMinutesPerDay) // fallback 16h * 60
    }

    @Test
    fun formatMoney_wholeNumberAmount() {
        assertEquals("€5.00", 5.0.formatMoney("€"))
    }

    @Test
    fun formatMoney_fractionalAmount() {
        assertEquals("$3.50", 3.50.formatMoney("$"))
    }

    @Test
    fun formatMoney_smallCents() {
        assertEquals("€0.05", 0.05.formatMoney("€"))
    }

    @Test
    fun formatMoney_zeroAmount() {
        assertEquals("€0.00", 0.0.formatMoney("€"))
    }

    @Test
    fun formatMoney_largerAmount() {
        assertEquals("$125.99", 125.99.formatMoney("$"))
    }

    @Test
    fun defaults_areReasonable() {
        val prefs = UserPreferences()
        assertEquals(0.0, prefs.packPrice)
        assertEquals(20, prefs.cigarettesPerPack)
        assertEquals(6, prefs.dayStartHour)
        assertEquals(22, prefs.bedtimeHour)
        assertEquals(null, prefs.manualDayStartEpochMillis)
        assertEquals(false, prefs.locationTrackingEnabled)
        assertEquals("€", prefs.currencySymbol)
        assertEquals(AccountTier.Free, prefs.accountTier)
        assertEquals(null, prefs.activeGoal)
    }
}

