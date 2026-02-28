package com.carefuldata.walktracker.data

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for UserPrefs data class.
 */
class UserPrefsTest {
    
    @Test
    fun isMetric_metricUnits_returnsTrue() {
        val prefs = UserPrefs(units = "metric")
        assertTrue(prefs.isMetric())
    }
    
    @Test
    fun isMetric_imperialUnits_returnsFalse() {
        val prefs = UserPrefs(units = "imperial")
        assertFalse(prefs.isMetric())
    }
    
    @Test
    fun getDistanceUnit_metric_returnsKm() {
        val prefs = UserPrefs(units = "metric")
        assertEquals("km", prefs.getDistanceUnit())
    }
    
    @Test
    fun getDistanceUnit_imperial_returnsMi() {
        val prefs = UserPrefs(units = "imperial")
        assertEquals("mi", prefs.getDistanceUnit())
    }
    
    @Test
    fun getSmallDistanceUnit_metric_returnsM() {
        val prefs = UserPrefs(units = "metric")
        assertEquals("m", prefs.getSmallDistanceUnit())
    }
    
    @Test
    fun getSmallDistanceUnit_imperial_returnsFt() {
        val prefs = UserPrefs(units = "imperial")
        assertEquals("ft", prefs.getSmallDistanceUnit())
    }
    
    @Test
    fun convertDistance_metric_returnsKilometers() {
        val prefs = UserPrefs(units = "metric")
        val result = prefs.convertDistance(1500.0) // 1500 meters
        assertEquals(1.5, result, 0.01)
    }
    
    @Test
    fun convertDistance_imperial_returnsMiles() {
        val prefs = UserPrefs(units = "imperial")
        val result = prefs.convertDistance(1609.34) // 1609.34 meters = 1 mile
        assertEquals(1.0, result, 0.01)
    }
    
    @Test
    fun convertToMeters_metric_returnsMeters() {
        val prefs = UserPrefs(units = "metric")
        val result = prefs.convertToMeters(1.5) // 1.5 km
        assertEquals(1500.0, result, 0.01)
    }
    
    @Test
    fun convertToMeters_imperial_returnsMeters() {
        val prefs = UserPrefs(units = "imperial")
        val result = prefs.convertToMeters(1.0) // 1 mile
        assertEquals(1609.34, result, 0.01)
    }
}
