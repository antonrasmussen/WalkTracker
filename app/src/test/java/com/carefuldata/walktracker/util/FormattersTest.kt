package com.carefuldata.walktracker.util

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Formatters utility class.
 */
class FormattersTest {
    
    @Test
    fun formatDistance_metric_shortDistance_returnsMeters() {
        val result = Formatters.formatDistance(500.0, isMetric = true)
        assertEquals("500 m", result)
    }
    
    @Test
    fun formatDistance_metric_longDistance_returnsKilometers() {
        val result = Formatters.formatDistance(1500.0, isMetric = true)
        assertEquals("1.5 km", result)
    }
    
    @Test
    fun formatDistance_imperial_shortDistance_returnsFeet() {
        val result = Formatters.formatDistance(500.0, isMetric = false)
        assertEquals("1640 ft", result)
    }
    
    @Test
    fun formatDistance_imperial_longDistance_returnsMiles() {
        val result = Formatters.formatDistance(1609.34, isMetric = false)
        assertEquals("1 mi", result)
    }
    
    @Test
    fun formatDuration_shortDuration_returnsMinutesSeconds() {
        val result = Formatters.formatDuration(125000) // 2 minutes 5 seconds
        assertEquals("02:05", result)
    }
    
    @Test
    fun formatDuration_longDuration_returnsHoursMinutesSeconds() {
        val result = Formatters.formatDuration(3661000) // 1 hour 1 minute 1 second
        assertEquals("01:01:01", result)
    }
    
    @Test
    fun formatSteps_largeNumber_returnsFormattedWithCommas() {
        val result = Formatters.formatSteps(12345)
        assertEquals("12,345", result)
    }
    
    @Test
    fun formatStepLength_metric_returnsCentimeters() {
        val result = Formatters.formatStepLength(0.78, isMetric = true)
        assertEquals("78 cm", result)
    }
    
    @Test
    fun formatStepLength_imperial_returnsInches() {
        val result = Formatters.formatStepLength(0.78, isMetric = false)
        assertEquals("30.71 in", result)
    }
    
    @Test
    fun formatSpeed_metric_returnsKmh() {
        val result = Formatters.formatSpeed(2.78, isMetric = true) // ~10 km/h
        assertEquals("10.01 km/h", result)
    }
    
    @Test
    fun formatSpeed_imperial_returnsMph() {
        val result = Formatters.formatSpeed(2.78, isMetric = false) // ~6.2 mph
        assertEquals("6.22 mph", result)
    }
    
    @Test
    fun formatPace_validDistance_returnsPace() {
        val result = Formatters.formatPace(3600000, 5000.0, isMetric = true) // 1 hour for 5km
        assertEquals("12:00", result)
    }
    
    @Test
    fun formatPace_zeroDistance_returnsPlaceholder() {
        val result = Formatters.formatPace(3600000, 0.0, isMetric = true)
        assertEquals("--:--", result)
    }
}
