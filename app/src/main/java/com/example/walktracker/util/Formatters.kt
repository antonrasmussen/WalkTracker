package com.example.walktracker.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for formatting various data types for display.
 */
object Formatters {
    
    private val distanceFormat = DecimalFormat("#.##")
    private val smallDistanceFormat = DecimalFormat("#")
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    /**
     * Format distance in meters to a user-friendly string.
     * 
     * @param meters Distance in meters
     * @param isMetric Whether to use metric (km) or imperial (miles) units
     * @return Formatted distance string
     */
    fun formatDistance(meters: Double, isMetric: Boolean = true): String {
        return if (isMetric) {
            val km = meters / 1000.0
            if (km >= 1.0) {
                "${distanceFormat.format(km)} km"
            } else {
                "${smallDistanceFormat.format(meters)} m"
            }
        } else {
            val miles = meters * 0.000621371
            if (miles >= 0.1) {
                "${distanceFormat.format(miles)} mi"
            } else {
                val feet = meters * 3.28084
                "${smallDistanceFormat.format(feet)} ft"
            }
        }
    }
    
    /**
     * Format duration in milliseconds to HH:mm:ss format.
     * 
     * @param durationMillis Duration in milliseconds
     * @return Formatted time string
     */
    fun formatDuration(durationMillis: Long): String {
        val seconds = durationMillis / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        } else {
            String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }
    
    /**
     * Format a timestamp to a readable date string.
     * 
     * @param timestampMillis Timestamp in milliseconds since epoch
     * @return Formatted date string
     */
    fun formatDate(timestampMillis: Long): String {
        return dateFormat.format(Date(timestampMillis))
    }
    
    /**
     * Format a timestamp to a readable date and time string.
     * 
     * @param timestampMillis Timestamp in milliseconds since epoch
     * @return Formatted date and time string
     */
    fun formatDateTime(timestampMillis: Long): String {
        return dateTimeFormat.format(Date(timestampMillis))
    }
    
    /**
     * Format step count with appropriate separators.
     * 
     * @param steps Number of steps
     * @return Formatted step count string
     */
    fun formatSteps(steps: Int): String {
        return String.format("%,d", steps)
    }
    
    /**
     * Format step length in meters to a readable string.
     * 
     * @param stepLengthMeters Step length in meters
     * @param isMetric Whether to use metric (cm) or imperial (inches) units
     * @return Formatted step length string
     */
    fun formatStepLength(stepLengthMeters: Double, isMetric: Boolean = true): String {
        return if (isMetric) {
            val cm = stepLengthMeters * 100
            "${distanceFormat.format(cm)} cm"
        } else {
            val inches = stepLengthMeters * 39.3701
            "${distanceFormat.format(inches)} in"
        }
    }
    
    /**
     * Format speed in meters per second to a user-friendly string.
     * 
     * @param speedMps Speed in meters per second
     * @param isMetric Whether to use metric (km/h) or imperial (mph) units
     * @return Formatted speed string
     */
    fun formatSpeed(speedMps: Double, isMetric: Boolean = true): String {
        return if (isMetric) {
            val kmh = speedMps * 3.6
            "${distanceFormat.format(kmh)} km/h"
        } else {
            val mph = speedMps * 2.23694
            "${distanceFormat.format(mph)} mph"
        }
    }
    
    /**
     * Format pace (time per distance unit) for walking/running.
     * 
     * @param durationMillis Duration in milliseconds
     * @param distanceMeters Distance in meters
     * @param isMetric Whether to use metric (min/km) or imperial (min/mi) units
     * @return Formatted pace string
     */
    fun formatPace(durationMillis: Long, distanceMeters: Double, isMetric: Boolean = true): String {
        if (distanceMeters <= 0) return "--:--"
        
        val durationSeconds = durationMillis / 1000.0
        val distanceUnit = if (isMetric) distanceMeters / 1000.0 else distanceMeters * 0.000621371
        val paceSecondsPerUnit = durationSeconds / distanceUnit
        
        val minutes = (paceSecondsPerUnit / 60).toInt()
        val seconds = (paceSecondsPerUnit % 60).toInt()
        
        return String.format("%d:%02d", minutes, seconds)
    }
}
